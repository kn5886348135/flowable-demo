package com.example.flowable.demo;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HolidayRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HolidayRequest.class);

    public static void main(String[] args) {
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:mysql:localhost:3306/flowable-demo")
                .setJdbcUsername("root")
                .setJdbcPassword("123456")
                .setJdbcDriver("com.mysql.jdbc.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);


        ProcessEngine processEngine = cfg.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        LOGGER.info("Found process definition {}", processDefinition.getName());
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("Who are you");
        String employee = scanner.nextLine();
        LOGGER.info("How Many hodidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());
        LOGGER.info("Why do you need them?");
        String description = scanner.nextLine();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holidayRequest", variables);

        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        LOGGER.info("You have {} tasks, they are {}", tasks.size(), tasks.stream().map(Task::getName).collect(Collectors.joining(", " + System.lineSeparator())));

        LOGGER.info("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        LOGGER.info("{} wants {} of holidays. Do you approve this?", processVariables.get("employee"), processVariables.get("nrOfHolidays"));

        boolean approved = scanner.nextLine().equalsIgnoreCase("Y");
        variables = new HashMap<>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);

        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstance.getId())
                .finished()
                .orderByHistoricActivityInstanceEndTime()
                .asc()
                .list();
        for (HistoricActivityInstance activity : activities) {
            LOGGER.info("{} took {} milliseconds", activity.getActivityId(), activity.getDurationInMillis());
        }

    }
}
