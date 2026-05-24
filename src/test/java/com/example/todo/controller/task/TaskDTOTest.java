package com.example.todo.controller.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskDTOTest {

    @Test
    void toDTO_TaskEntityをTaskDTOに変換できる() {
        //Arrange: TaskEntityを用意する
        TaskEntity entity = new TaskEntity(1L, "買い物", "牛乳と卵を買う", TaskStatus.TODO);

        //Act: テスト対象を実行
        TaskDTO actual = TaskDTO.toDTO(entity);

        //Assert
        assertEquals(1L, actual.id());
        assertEquals("買い物", actual.summary());
        assertEquals("牛乳と卵を買う", actual.description());
        assertEquals("TODO", actual.status());
    }

    @Test
    void toDTO_ステータスがDOINGの場合_文字列DOINGになる() {
        //Arrange
        TaskEntity entity = new TaskEntity(2L, "資料作成", "提案書を書く", TaskStatus.DOING);

        //Act
        TaskDTO actual = TaskDTO.toDTO(entity);

        //Assert
        assertEquals("DOING", actual.status());
    }

    @Test
    void toDTO_ステータスがDONEの場合_文字列DONEになる() {
        //Arrange
        TaskEntity entity = new TaskEntity(3L, "報告書", "提出済み", TaskStatus.DONE);

        //Act
        TaskDTO actual = TaskDTO.toDTO(entity);

        //Assert
        assertEquals("DONE", actual.status());
    }
}