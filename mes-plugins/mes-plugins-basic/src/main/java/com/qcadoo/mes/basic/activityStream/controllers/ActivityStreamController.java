package com.qcadoo.mes.basic.activityStream.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qcadoo.mes.basic.activityStream.ActivityStreamService;
import com.qcadoo.mes.basic.activityStream.model.ActivityDto;

@Controller
@RequestMapping("/activityStream")
public class ActivityStreamController {

    @Autowired
    private ActivityStreamService activityStreamService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActivityDto> getActivityStream() {
        return activityStreamService.getActivityStream();
    }

    @ResponseBody
    @RequestMapping(value = "/markAsViewed", method = RequestMethod.POST)
    public void markActivityAsViewed(@RequestBody final List<Integer> viewedActivities) {
        activityStreamService.markActivityAsViewed(viewedActivities);
    }
}
