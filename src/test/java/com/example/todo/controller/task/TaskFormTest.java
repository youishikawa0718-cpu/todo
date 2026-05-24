package com.example.todo.controller.task;

import com.example.todo.service.task.TaskEntity;
import com.example.todo.service.task.TaskStatus;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.parser.Entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskFormTest {

    @Test
    void fromEntity_TaskEntityをTaskFormに変換できる() {
        // Arrange: テスト対象に渡す入力を用意する
        TaskEntity entity = new TaskEntity(1L, "買い物", "牛乳と卵を買う", TaskStatus.TODO);

        // Act: テスト対象のメソッドを実行する
        TaskForm actual = TaskForm.fromEntity(entity);

        // Assert: 期待する結果になっているか検証する
        assertEquals("買い物", actual.summary());
        assertEquals("牛乳と卵を買う", actual.description());
        assertEquals("TODO", actual.status());
    }

    @Test
    void toEntity_TaskFormをTaskEntityに変換できる_idなし() {
        //Arrange: TaskFormを用意する (statusはStringで渡す)
        TaskForm form = new TaskForm("買い物", "牛乳と卵を買う","TODO");

        //Act: テスト対象を実行
        TaskEntity actual = form.toEntity();

        //Assert: 中身を検証する
        assertEquals(null, actual.id());
        assertEquals("買い物", actual.summary());
        assertEquals("牛乳と卵を買う", actual.description());
        assertEquals(TaskStatus.TODO, actual.status());
    }

    @Test
    void toEntity_TaskFormをTaskEntityに変換できる_id指定あり() {
        //Arrange
        TaskForm form = new TaskForm("買い物", "牛乳と卵を買う", "DOING");

        //Act: 引数でidを渡すバージョン
        TaskEntity actual = form.toEntity(42L);

        //Assert
        assertEquals(42L, actual.id());
        assertEquals("買い物", actual.summary());
        assertEquals("牛乳と卵を買う", actual.description());
        assertEquals(TaskStatus.DOING, actual.status());
    }
}

