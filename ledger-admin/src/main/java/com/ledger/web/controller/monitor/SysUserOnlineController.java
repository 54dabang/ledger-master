package com.ledger.web.controller.monitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ledger.common.annotation.Log;
import com.ledger.common.constant.CacheConstants;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.core.page.TableDataInfo;
import com.ledger.common.core.redis.RedisCache;
import com.ledger.common.enums.BusinessType;
import com.ledger.common.utils.StringUtils;
import com.ledger.system.domain.SysUserOnline;
import com.ledger.system.service.ISysUserOnlineService;

/**
 * 在线用户监控
 *
 * @author ledger
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController
{
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName)
    {
        final Map<String, SysUserOnline> latestOnlineUserMap = new LinkedHashMap<String, SysUserOnline>();
        redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*", 1000, key -> {
            LoginUser user = redisCache.getCacheObject(key);
            if (StringUtils.isNull(user))
            {
                return;
            }
            SysUserOnline userOnline = null;
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                userOnline = userOnlineService.selectOnlineByInfo(ipaddr, userName, user);
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                userOnline = userOnlineService.selectOnlineByIpaddr(ipaddr, user);
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                userOnline = userOnlineService.selectOnlineByUserName(userName, user);
            }
            else
            {
                userOnline = userOnlineService.loginUserToUserOnline(user);
            }
            retainLatestOnlineUser(latestOnlineUserMap, userOnline);
        });
        return getDataTable(sortOnlineUsers(latestOnlineUserMap));
    }

    private void retainLatestOnlineUser(Map<String, SysUserOnline> latestOnlineUserMap, SysUserOnline userOnline)
    {
        if (StringUtils.isNull(userOnline))
        {
            return;
        }
        String userName = userOnline.getUserName();
        String uniqueKey = StringUtils.isNotEmpty(userName) ? userName : userOnline.getTokenId();
        SysUserOnline latestOnlineUser = latestOnlineUserMap.get(uniqueKey);
        if (StringUtils.isNull(latestOnlineUser) || compareLoginTime(userOnline, latestOnlineUser) > 0)
        {
            latestOnlineUserMap.put(uniqueKey, userOnline);
        }
    }

    private List<SysUserOnline> sortOnlineUsers(Map<String, SysUserOnline> latestOnlineUserMap)
    {
        List<SysUserOnline> latestOnlineUserList = new ArrayList<SysUserOnline>(latestOnlineUserMap.values());
        latestOnlineUserList.sort(new Comparator<SysUserOnline>()
        {
            @Override
            public int compare(SysUserOnline firstUser, SysUserOnline secondUser)
            {
                return compareLoginTime(secondUser, firstUser);
            }
        });
        return latestOnlineUserList;
    }

    private int compareLoginTime(SysUserOnline firstUser, SysUserOnline secondUser)
    {
        Long firstLoginTime = firstUser.getLoginTime();
        Long secondLoginTime = secondUser.getLoginTime();
        if (StringUtils.isNull(firstLoginTime) && StringUtils.isNull(secondLoginTime))
        {
            return 0;
        }
        if (StringUtils.isNull(firstLoginTime))
        {
            return -1;
        }
        if (StringUtils.isNull(secondLoginTime))
        {
            return 1;
        }
        return firstLoginTime.compareTo(secondLoginTime);
    }

    /**
     * 强退用户
     */
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
        redisCache.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return success();
    }
}
