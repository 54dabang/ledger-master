package com.ledger.quartz.task;

import com.ledger.business.service.SyncBimUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("syncBimUserTask")
public class SyncBimUserTask {
    @Autowired
    private SyncBimUserService syncBimUserService;
    public void execute(){
        syncBimUserService.syncUsersAndDepts();
    }

}
