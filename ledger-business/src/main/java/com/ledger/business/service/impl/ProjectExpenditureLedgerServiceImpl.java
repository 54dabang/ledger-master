package com.ledger.business.service.impl;

import com.ledger.business.domain.CtgLedgerAnnualBudget;
import com.ledger.business.domain.CtgLedgerProject;
import com.ledger.business.domain.CtgLedgerProjectExpenseDetail;
import com.ledger.business.mapper.CtgLedgerAnnualBudgetMapper;
import com.ledger.business.mapper.CtgLedgerProjectExpenseDetailMapper;
import com.ledger.business.mapper.CtgLedgerProjectMapper;
import com.ledger.business.service.IProjectExpenditureLedgerService;
import com.ledger.business.util.StrUtil;
import com.ledger.business.vo.CategoryEnum;
import com.ledger.business.vo.ProjectExpenditureColumnEnum;
import com.ledger.business.vo.ProjectExpenditureLedgerColumnVo;
import com.ledger.business.vo.ProjectExpenditureLedgerVo;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.system.service.ISysUserService;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ProjectExpenditureLedgerServiceImpl implements IProjectExpenditureLedgerService {
    @Autowired
    private CtgLedgerProjectMapper ctgLedgerProjectMapper;
    @Autowired
    private CtgLedgerAnnualBudgetMapper CtgLedgerAnnualBudgetMapper;
    @Autowired
    private CtgLedgerProjectExpenseDetailMapper ctgLedgerProjectExpenseDetailMapper;
    @Autowired
    private ISysUserService sysUserService;


    @Override
    public ProjectExpenditureLedgerVo getProjectExpenditureLedgerVo(Long projectId, Integer year, Long reimbursementSequenceNo) {
        CtgLedgerProject ctgLedgerProject = ctgLedgerProjectMapper.selectCtgLedgerProjectById(projectId);
        CtgLedgerAnnualBudget annualBudget = CtgLedgerAnnualBudgetMapper.selectByProjectIdAndYear(projectId, year);
        if (Objects.isNull(annualBudget)) {
            throw new IllegalStateException("年度预算不存在，请联系管理员新增！");
        }
        List<CtgLedgerProjectExpenseDetail> detailList = ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailListByProjectIdAndYear(projectId, year);
        detailList = detailList.stream().filter(d -> d.getReimbursementSequenceNo() <= reimbursementSequenceNo).collect(Collectors.toList());


        //项目经费执行情况
        ProjectExpenditureLedgerColumnVo totalBudgetCol = convertToLedgerColumnVo(ctgLedgerProject);
        ProjectExpenditureLedgerColumnVo executedAmountCol = convertToExecutedAmountLedgerColumnVo(ctgLedgerProject);

        //年度项目经费执行情况
        //年度预算经费
        ProjectExpenditureLedgerColumnVo annualBudgetCol = buildAnnualBudget(annualBudget);

        //历史所有支出
        //将detailList转为按照reimbursementSequenceNo 排序后的有序map（按照值正排），key为reimbursementSequenceNo，value为为reimbursementSequenceNo相等的CtgLedgerProjectExpenseDetail列表，该列表按照CtgLedgerProjectExpenseDetail.createTime倒排
        LinkedHashMap<Long, List<CtgLedgerProjectExpenseDetail>> sortedCtgLedgerProjectExpenseDetailMap = detailList.stream()
                .collect(Collectors.groupingBy(
                        CtgLedgerProjectExpenseDetail::getReimbursementSequenceNo,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted((d1, d2) -> d2.getCreateTime().compareTo(d1.getCreateTime()))
                                .collect(Collectors.toList()),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
        //累计支出经费
        ProjectExpenditureLedgerColumnVo totalCumulativeExpenditureColVo = sumHistory(sortedCtgLedgerProjectExpenseDetailMap, ProjectExpenditureColumnEnum.CUMULATIVE_FUNDS);

        //本次所有支出
        Map.Entry<Long, List<CtgLedgerProjectExpenseDetail>> lastEntry = sortedCtgLedgerProjectExpenseDetailMap
                .entrySet()
                .stream()
                .reduce((first, second) -> second)
                .orElse(null);
        //本次支出经费
        ProjectExpenditureLedgerColumnVo currentExpenditureColVo = sumHistory(lastEntry.getValue(), ProjectExpenditureColumnEnum.CURRENT_FUNDS);


        //除本次之外的所有支出
        if (lastEntry != null) {
            sortedCtgLedgerProjectExpenseDetailMap.remove(lastEntry.getKey());
        }
        ProjectExpenditureLedgerColumnVo cumulativeExpenditureExceptCurrentCol = sumHistory(sortedCtgLedgerProjectExpenseDetailMap, null);


        ProjectExpenditureLedgerColumnVo lastRemainingExpenditure = buildLastRemainingFunds(annualBudgetCol, cumulativeExpenditureExceptCurrentCol);
        ProjectExpenditureLedgerColumnVo currentRemainingExpenditure = buildLastRemainingFunds(annualBudgetCol, totalCumulativeExpenditureColVo);
        ProjectExpenditureLedgerVo projectExpenditureLedgerVo = ProjectExpenditureLedgerVo.builder().totalBudget(totalBudgetCol)
                .executedAmount(executedAmountCol)
                .annualBudget(annualBudgetCol)
                .lastRemainingFunds(lastRemainingExpenditure)
                .currentFunds(currentExpenditureColVo)
                .cumulativeFunds(totalCumulativeExpenditureColVo)
                .currentRemainingFunds(currentRemainingExpenditure)
                .build();
        BigDecimal remainingTotalFunds = Stream.of(currentRemainingExpenditure.getDirectCosts(), currentRemainingExpenditure.getIndirectCosts(), currentRemainingExpenditure.getContractAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        projectExpenditureLedgerVo.setRemainingTotalFunds(remainingTotalFunds);
        projectExpenditureLedgerVo.setProjectName(ctgLedgerProject.getProjectName());
        projectExpenditureLedgerVo.setYear(year);
        projectExpenditureLedgerVo.setSequenceNo(reimbursementSequenceNo);
        projectExpenditureLedgerVo.setRemarkList(buildRemark(lastEntry.getValue()));

        return projectExpenditureLedgerVo;
    }

    public static List<String> buildRemark(List<CtgLedgerProjectExpenseDetail> expenseDetails) {
        return expenseDetails.stream().map(e -> StrUtil.buildRemarkInYuan(e, true)).collect(Collectors.toList());
    }


    private ProjectExpenditureLedgerColumnVo buildLastRemainingFunds(ProjectExpenditureLedgerColumnVo annualBudgetCol, ProjectExpenditureLedgerColumnVo cumulativeExpenditureExceptCurrent) {
        ProjectExpenditureLedgerColumnVo lastRemainingExpenditure = ProjectExpenditureLedgerColumnVo.builder()
                .equipPurchaseFee(safeSubtract(annualBudgetCol.getEquipPurchaseFee(), cumulativeExpenditureExceptCurrent.getEquipPurchaseFee()))
                .protoEquipFee(safeSubtract(annualBudgetCol.getProtoEquipFee(), cumulativeExpenditureExceptCurrent.getProtoEquipFee()))
                .equipRenovFee(safeSubtract(annualBudgetCol.getEquipRenovFee(), cumulativeExpenditureExceptCurrent.getEquipRenovFee()))
                .equipRentFee(safeSubtract(annualBudgetCol.getEquipRentFee(), cumulativeExpenditureExceptCurrent.getEquipRentFee()))
                .materialCost(safeSubtract(annualBudgetCol.getMaterialCost(), cumulativeExpenditureExceptCurrent.getMaterialCost()))
                .testProcFee(safeSubtract(annualBudgetCol.getTestProcFee(), cumulativeExpenditureExceptCurrent.getTestProcFee()))
                .fuelPowerCost(safeSubtract(annualBudgetCol.getFuelPowerCost(), cumulativeExpenditureExceptCurrent.getFuelPowerCost()))
                .pubDocIpFee(safeSubtract(annualBudgetCol.getPubDocIpFee(), cumulativeExpenditureExceptCurrent.getPubDocIpFee()))
                .travelConfCoopFee(safeSubtract(annualBudgetCol.getTravelConfCoopFee(), cumulativeExpenditureExceptCurrent.getTravelConfCoopFee()))
                .laborCost(safeSubtract(annualBudgetCol.getLaborCost(), cumulativeExpenditureExceptCurrent.getLaborCost()))
                .serviceCost(safeSubtract(annualBudgetCol.getServiceCost(), cumulativeExpenditureExceptCurrent.getServiceCost()))
                .expertConsultFee(safeSubtract(annualBudgetCol.getExpertConsultFee(), cumulativeExpenditureExceptCurrent.getExpertConsultFee()))
                .mgmtFee(safeSubtract(annualBudgetCol.getMgmtFee(), cumulativeExpenditureExceptCurrent.getMgmtFee()))
                .taxFee(safeSubtract(annualBudgetCol.getTaxFee(), cumulativeExpenditureExceptCurrent.getTaxFee()))
                .contractAmount(safeSubtract(annualBudgetCol.getContractAmount(), cumulativeExpenditureExceptCurrent.getContractAmount()))
                .build();

        // 计算直接费用和间接费用
        lastRemainingExpenditure.setDirectCosts(buildDirectCosts(lastRemainingExpenditure));
        lastRemainingExpenditure.setIndirectCosts(buildInDirectCosts(lastRemainingExpenditure));
        lastRemainingExpenditure.setColumnCnName(ProjectExpenditureColumnEnum.LAST_REMAINING_FUNDS.cnName());
        lastRemainingExpenditure.setColumnEngName(ProjectExpenditureColumnEnum.LAST_REMAINING_FUNDS.engName());
        return lastRemainingExpenditure;
    }

    // 安全减法，避免空指针异常
    private BigDecimal safeSubtract(BigDecimal minuend, BigDecimal subtrahend) {
        if (minuend == null) {
            minuend = BigDecimal.ZERO;
        }
        if (subtrahend == null) {
            subtrahend = BigDecimal.ZERO;
        }
        return minuend.subtract(subtrahend);
    }

    /**
     * 累计支出
     *
     * @param sortedCtgLedgerProjectExpenseDetailMap
     * @return
     */
    public ProjectExpenditureLedgerColumnVo sumHistory(LinkedHashMap<Long, List<CtgLedgerProjectExpenseDetail>> sortedCtgLedgerProjectExpenseDetailMap, ProjectExpenditureColumnEnum columnEnum) {
        List<CtgLedgerProjectExpenseDetail> detailList = toList(sortedCtgLedgerProjectExpenseDetailMap);
        return sumHistory(detailList, columnEnum);
    }

    public ProjectExpenditureLedgerColumnVo sumHistory(List<CtgLedgerProjectExpenseDetail> detailList, ProjectExpenditureColumnEnum columnEnum) {
        BigDecimal equipPurchaseFee = sumByCategoryEnum(CategoryEnum.EQUIP_PURCHASE_FEE, detailList);
        BigDecimal protoEquipFee = sumByCategoryEnum(CategoryEnum.PROTO_EQUIP_FEE, detailList);
        BigDecimal equipRenovFee = sumByCategoryEnum(CategoryEnum.EQUIP_RENOV_FEE, detailList);
        BigDecimal equipRentFee = sumByCategoryEnum(CategoryEnum.EQUIP_RENT_FEE, detailList);

        //材料费
        BigDecimal materialCost = sumByCategoryEnum(CategoryEnum.MATERIAL_COST, detailList);
        //测试化验加工费
        BigDecimal testProcFee = sumByCategoryEnum(CategoryEnum.TEST_PROC_FEE, detailList);
        BigDecimal fuelPowerCost = sumByCategoryEnum(CategoryEnum.FUEL_POWER_COST, detailList);
        BigDecimal pubDocIpFee = sumByCategoryEnum(CategoryEnum.PUB_DOC_IP_FEE, detailList);
        //差旅/会议/国际合作交流费
        BigDecimal travelConfCoopFee = sumByCategoryEnum(CategoryEnum.TRAVEL_CONF_COOP_FEE, detailList);
        BigDecimal laborCost = sumByCategoryEnum(CategoryEnum.LABOR_COST, detailList);
        BigDecimal serviceCost = sumByCategoryEnum(CategoryEnum.SERVICE_COST, detailList);
        //专家咨询费
        BigDecimal expertConsultFee = sumByCategoryEnum(CategoryEnum.EXPERT_CONSULT_FEE, detailList);

        BigDecimal mgmtFee = sumByCategoryEnum(CategoryEnum.MGMT_FEE, detailList);
        BigDecimal taxFee = sumByCategoryEnum(CategoryEnum.TAX_FEE, detailList);
        //合同金额
        BigDecimal contractAmount = sumByCategoryEnum(CategoryEnum.CONTRACT_AMOUNT, detailList);

        ProjectExpenditureLedgerColumnVo sumHistory = ProjectExpenditureLedgerColumnVo.builder()
                .equipPurchaseFee(equipPurchaseFee)
                .protoEquipFee(protoEquipFee)
                .equipRenovFee(equipRenovFee)
                .equipRentFee(equipRentFee)
                .materialCost(materialCost)
                .testProcFee(testProcFee)
                .fuelPowerCost(fuelPowerCost)
                .pubDocIpFee(pubDocIpFee)
                .travelConfCoopFee(travelConfCoopFee)
                .laborCost(laborCost)
                .serviceCost(serviceCost)
                .expertConsultFee(expertConsultFee)
                .mgmtFee(mgmtFee)
                .taxFee(taxFee)
                .contractAmount(contractAmount)
                .build();

        BigDecimal directCosts = buildDirectCosts(sumHistory);
        BigDecimal insDirectCosts = buildInDirectCosts(sumHistory);

        //计算直接费用和间接费用
        sumHistory.setDirectCosts(directCosts);
        sumHistory.setIndirectCosts(insDirectCosts);
        if (columnEnum != null) {
            sumHistory.setColumnCnName(columnEnum.cnName());
            sumHistory.setColumnEngName(columnEnum.engName());
        }


        return sumHistory;
    }

    private BigDecimal sumByCategoryEnum(CategoryEnum categoryEnum, List<CtgLedgerProjectExpenseDetail> detailList) {
        return detailList.stream().filter(d -> d.getSubjectName().contains(categoryEnum.cnName())).map(d -> d.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<CtgLedgerProjectExpenseDetail> toList(LinkedHashMap<Long, List<CtgLedgerProjectExpenseDetail>> sortedCtgLedgerProjectExpenseDetailMap) {
        return sortedCtgLedgerProjectExpenseDetailMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 构造项目直接费用
     *
     * @param col
     * @return
     */
    private BigDecimal buildDirectCosts(ProjectExpenditureLedgerColumnVo col) {


        BigDecimal equipFee = buildEquipFee(col);
        BigDecimal othersFee = buildOthersFee(col);

        return Stream.of(equipFee, othersFee).reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * 设备费
     *
     * @param col
     * @return
     */
    private BigDecimal buildEquipFee(ProjectExpenditureLedgerColumnVo col) {
        BigDecimal equipFee = Stream.of(col.getEquipPurchaseFee(),
                        col.getProtoEquipFee(),
                        col.getEquipRenovFee(),
                        col.getEquipRentFee())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return equipFee;
    }

    /**
     * 除设备费以外的其他直接费用
     *
     * @param col
     * @return
     */
    private BigDecimal buildOthersFee(ProjectExpenditureLedgerColumnVo col) {
        BigDecimal othersFee = Stream.of(
                col.getMaterialCost(),
                col.getTestProcFee(),
                col.getFuelPowerCost(),
                col.getPubDocIpFee(),
                col.getTravelConfCoopFee(),
                col.getLaborCost(),
                col.getServiceCost(),
                col.getExpertConsultFee()
        ).reduce(BigDecimal.ZERO, BigDecimal::add);


        return othersFee;
    }

    /**
     * 间接费用
     *
     * @param col
     * @return
     */
    public BigDecimal buildInDirectCosts(ProjectExpenditureLedgerColumnVo col) {
        BigDecimal inDirectCosts = Stream.of(
                col.getMgmtFee(),
                col.getTaxFee()
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
        return inDirectCosts;
    }


    private ProjectExpenditureLedgerColumnVo buildAnnualBudget(CtgLedgerAnnualBudget annualBudget) {
        ProjectExpenditureLedgerColumnVo annualBudgetCol = ProjectExpenditureLedgerColumnVo.builder()
                .equipPurchaseFee(annualBudget.getEquipPurchaseFee())
                .protoEquipFee(annualBudget.getProtoEquipFee())
                .equipRenovFee(annualBudget.getEquipRenovFee())
                .equipRentFee(annualBudget.getEquipRentFee())
                .materialCost(annualBudget.getMaterialCost())
                .testProcFee(annualBudget.getTestProcFee())
                .fuelPowerCost(annualBudget.getFuelPowerCost())
                .pubDocIpFee(annualBudget.getPubDocIpFee())
                .travelConfCoopFee(annualBudget.getTravelConfCoopFee())
                .laborCost(annualBudget.getLaborCost())
                .serviceCost(annualBudget.getServiceCost())
                .expertConsultFee(annualBudget.getExpertConsultFee())
                .mgmtFee(annualBudget.getMgmtFee())
                .taxFee(annualBudget.getTaxFee())
                .contractAmount(annualBudget.getContractAmount())
                .build();

        annualBudgetCol.setDirectCosts(buildDirectCosts(annualBudgetCol));
        annualBudgetCol.setIndirectCosts(buildInDirectCosts(annualBudgetCol));
        annualBudgetCol.setColumnCnName(ProjectExpenditureColumnEnum.ANNUAL_BUDGET.cnName());
        annualBudgetCol.setColumnEngName(ProjectExpenditureColumnEnum.ANNUAL_BUDGET.engName());

        return annualBudgetCol;
    }

    private ProjectExpenditureLedgerColumnVo convertToLedgerColumnVo(CtgLedgerProject ctgLedgerProject) {
        ProjectExpenditureLedgerColumnVo projectExpenditureLedgerColumnVo = ProjectExpenditureLedgerColumnVo.builder()
                .projectId(ctgLedgerProject.getId())
                .projectName(ctgLedgerProject.getProjectName())
                .projectCode(ctgLedgerProject.getProjectCode())
                .extendProjectId(ctgLedgerProject.getExtendProjectId())
                .projectManagerLoginName(ctgLedgerProject.getProjectManagerLoginName())
                .equipPurchaseFee(ctgLedgerProject.getEquipPurchaseFee())
                .protoEquipFee(ctgLedgerProject.getProtoEquipFee())
                .equipRenovFee(ctgLedgerProject.getEquipRenovFee())
                .equipRentFee(ctgLedgerProject.getEquipRentFee())
                .materialCost(ctgLedgerProject.getMaterialCost())
                .testProcFee(ctgLedgerProject.getTestProcFee())
                .fuelPowerCost(ctgLedgerProject.getFuelPowerCost())
                .pubDocIpFee(ctgLedgerProject.getPubDocIpFee())
                .travelConfCoopFee(ctgLedgerProject.getTravelConfCoopFee())
                .laborCost(ctgLedgerProject.getLaborCost())
                .serviceCost(ctgLedgerProject.getServiceCost())
                .expertConsultFee(ctgLedgerProject.getExpertConsultFee())
                .mgmtFee(ctgLedgerProject.getMgmtFee())
                .taxFee(ctgLedgerProject.getTaxFee())
                .contractAmount(ctgLedgerProject.getContractAmount())
                .build();


        projectExpenditureLedgerColumnVo.setDirectCosts(buildDirectCosts(projectExpenditureLedgerColumnVo));
        projectExpenditureLedgerColumnVo.setIndirectCosts(buildInDirectCosts(projectExpenditureLedgerColumnVo));
        projectExpenditureLedgerColumnVo.setColumnCnName(ProjectExpenditureColumnEnum.TOTAL_BUDGET.cnName());
        projectExpenditureLedgerColumnVo.setColumnEngName(ProjectExpenditureColumnEnum.TOTAL_BUDGET.engName());

        return projectExpenditureLedgerColumnVo;
    }

    /**
     * 构造已经执行金额
     *
     * @param ctgLedgerProject
     * @return
     */
    private ProjectExpenditureLedgerColumnVo convertToExecutedAmountLedgerColumnVo(CtgLedgerProject ctgLedgerProject) {
        ProjectExpenditureLedgerColumnVo projectExpenditureLedgerColumnVo = ProjectExpenditureLedgerColumnVo.builder()
                .projectId(ctgLedgerProject.getId())
                .projectName(ctgLedgerProject.getProjectName())
                .projectCode(ctgLedgerProject.getProjectCode())
                .extendProjectId(ctgLedgerProject.getExtendProjectId())
                .projectManagerLoginName(ctgLedgerProject.getProjectManagerLoginName())
                .equipPurchaseFee(ctgLedgerProject.getExecutedEquipPurchaseFee())
                .protoEquipFee(ctgLedgerProject.getExecutedProtoEquipFee())
                .equipRenovFee(ctgLedgerProject.getExecutedEquipRenovFee())
                .equipRentFee(ctgLedgerProject.getExecutedEquipRentFee())
                .materialCost(ctgLedgerProject.getExecutedMaterialCost())
                .testProcFee(ctgLedgerProject.getExecutedTestProcFee())
                .fuelPowerCost(ctgLedgerProject.getExecutedFuelPowerCost())
                .pubDocIpFee(ctgLedgerProject.getExecutedPubDocIpFee())
                .travelConfCoopFee(ctgLedgerProject.getExecutedTravelConfCoopFee())
                .laborCost(ctgLedgerProject.getExecutedLaborCost())
                .serviceCost(ctgLedgerProject.getExecutedServiceCost())
                .expertConsultFee(ctgLedgerProject.getExecutedExpertConsultFee())
                .mgmtFee(ctgLedgerProject.getExecutedMgmtFee())
                .taxFee(ctgLedgerProject.getExecutedTaxFee())
                .contractAmount(ctgLedgerProject.getExecutedContractAmount())
                .build();


        projectExpenditureLedgerColumnVo.setDirectCosts(buildDirectCosts(projectExpenditureLedgerColumnVo));
        projectExpenditureLedgerColumnVo.setIndirectCosts(buildInDirectCosts(projectExpenditureLedgerColumnVo));
        projectExpenditureLedgerColumnVo.setColumnCnName(ProjectExpenditureColumnEnum.EXECUTED_AMOUNT.cnName());
        projectExpenditureLedgerColumnVo.setColumnEngName(ProjectExpenditureColumnEnum.EXECUTED_AMOUNT.engName());

        return projectExpenditureLedgerColumnVo;
    }

    @Override
    public Long selectMaxReimbursementSequenceNo(Long projectId, Integer year) {
        return ctgLedgerProjectExpenseDetailMapper.selectMaxReimbursementSequenceNoByProjectIdAndYear(projectId, year);
    }

    @Override
    public boolean projectExpenditureLedgerValid(Long projectId, Integer year, Long reimbursementSequenceNo) {
        CtgLedgerProject ctgLedgerProject = ctgLedgerProjectMapper.selectCtgLedgerProjectById(projectId);
        CtgLedgerAnnualBudget annualBudget = CtgLedgerAnnualBudgetMapper.selectByProjectIdAndYear(projectId, year);
        if (Objects.isNull(annualBudget)) {
            throw new IllegalStateException("年度预算不存在，请联系管理员新增！");
        }
        // 项目管理员
        SysUser projectManager = Optional.ofNullable(ctgLedgerProject)
                .map(p -> p.getProjectManagerLoginName())
                .map(sysUserService::selectUserByUserName)
                .orElseThrow(() -> new IllegalStateException("项目管理员信息不存在"));

        CtgLedgerProjectExpenseDetail queryParam = new CtgLedgerProjectExpenseDetail();
        queryParam.setLedgerProjectId(projectId);
        queryParam.setYear(year);
        queryParam.setReimbursementSequenceNo(reimbursementSequenceNo);
        List<CtgLedgerProjectExpenseDetail> projectExpenseDetailList = ctgLedgerProjectExpenseDetailMapper.selectCtgLedgerProjectExpenseDetailList(queryParam);

        String reimburserLoginName = Optional.ofNullable(projectExpenseDetailList.get(0))
                .map(e -> e.getReimburserLoginName())
                .orElse(null);


        // 报销人信息校验（前提条件）
        SysUser reimburser = Optional.ofNullable(reimburserLoginName)
                .map(sysUserService::selectUserByUserName)
                .orElseThrow(() -> new IllegalStateException("报销人信息不存在"));

        // 核心信息提取（极简变量名，兼顾简洁与可读性）
        String pm = projectManager.getNickName();
        String rb = reimburser.getNickName();
        boolean pmNoSig = StringUtils.isEmpty(projectManager.getSignaturePic());
        boolean rbNoSig = StringUtils.isEmpty(reimburser.getSignaturePic());
        String suffix = "尚未上传自己的电子签名，请维护！";

        // 构建异常信息（无冗余拼接，逻辑直观）
        if (pmNoSig || rbNoSig) {
            String errMsg = pmNoSig && rbNoSig
                    ? String.format("项目管理员:%s和报销人:%s%s", pm, rb, suffix)
                    : String.format("%s:%s%s", pmNoSig ? "项目管理员" : "报销人", pmNoSig ? pm : rb, suffix);
            throw new IllegalStateException(errMsg);
        }

        return false;
    }
}
