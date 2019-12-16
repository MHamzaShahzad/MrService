package com.example.mrservice.interfaces;

import com.example.mrservice.models.TaskModel;

import java.util.List;

public interface OnTasksListUpdateI {

    void onTaskListUpdate(List<TaskModel> taskModelList);

}
