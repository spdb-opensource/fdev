package com.spdb.fdev.freport.base.utils;

import com.spdb.fdev.freport.base.dict.TaskEnum;
import com.spdb.fdev.freport.spdb.entity.task.Task;

public class TaskUtils {

    public static void set(Task task) {
        if (!CommonUtils.isNullOrEmpty(task.getStage())) {
            switch (task.getStage()) {
                case "develop":
                    task.setStage(TaskEnum.TaskStage.DEVELOP.getValue());
                    break;
                case "sit":
                    task.setStage(TaskEnum.TaskStage.SIT.getValue());
                    break;
                case "uat":
                    task.setStage(TaskEnum.TaskStage.UAT.getValue());
                    break;
                case "rel":
                    task.setStage(TaskEnum.TaskStage.REL.getValue());
                    break;
                case "production":
                    task.setStage(TaskEnum.TaskStage.PRODUCTION.getValue());
                    break;
                case "file":
                    task.setStage(TaskEnum.TaskStage.FILE.getValue());
                    break;
            }
        }
//        String diffDesc = task.getDifficultyDesc();
//        if (!CommonUtils.isNullOrEmpty(diffDesc)) {
//            String diffDescFront = diffDesc.substring(0, 1);
//            switch (diffDescFront) {
//                case "A":
//                    task.setDevelopWay(TaskEnum.DevelopWayEnum.A.getValue());
//                    break;
//                case "B":
//                    task.setDevelopWay(TaskEnum.DevelopWayEnum.B.getValue());
//                    break;
//                case "C":
//                    task.setDevelopWay(TaskEnum.DevelopWayEnum.C.getValue());
//                    break;
//                case "D":
//                    task.setDevelopWay(TaskEnum.DevelopWayEnum.D.getValue());
//                    break;
//            }
//            switch (diffDesc){
//                case "A1":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A1.getValue());
//                    break;
//                case "A2":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A2.getValue());
//                    break;
//                case "A3":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A3.getValue());
//                    break;
//                case "A4":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A4.getValue());
//                    break;
//                case "A5":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A5.getValue());
//                    break;
//                case "A6":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.A6.getValue());
//                    break;
//                case "B1":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B1.getValue());
//                    break;
//                case "B2":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B2.getValue());
//                    break;
//                case "B3":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B3.getValue());
//                    break;
//                case "B4":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B4.getValue());
//                    break;
//                case "B5":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B5.getValue());
//                    break;
//                case "B6":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.B6.getValue());
//                    break;
//                case "C1":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C1.getValue());
//                    break;
//                case "C2":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C2.getValue());
//                    break;
//                case "C3":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C3.getValue());
//                    break;
//                case "C4":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C4.getValue());
//                    break;
//                case "C5":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C5.getValue());
//                    break;
//                case "C6":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.C6.getValue());
//                    break;
//                case "D1":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D1.getValue());
//                    break;
//                case "D2":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D2.getValue());
//                    break;
//                case "D3":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D3.getValue());
//                    break;
//                case "D4":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D4.getValue());
//                    break;
//                case "D5":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D5.getValue());
//                    break;
//                case "D6":
//                    task.setDiffDesc(TaskEnum.TaskDiffEnum.D6.getValue());
//                    break;
//            }
//        }
    }

}
