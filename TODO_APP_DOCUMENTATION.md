# TODOアプリケーション 完全解説ガイド

## 目次
1. [アプリケーション全体構造](#アプリケーション全体構造)
2. [レイヤーアーキテクチャ](#レイヤーアーキテクチャ)
3. [主要ファイル解説](#主要ファイル解説)
4. [データの流れ](#データの流れ)
5. [まとめ](#まとめ)

---

## アプリケーション全体構造

このTODOアプリケーションは、Spring Bootを使った標準的な3層アーキテクチャで構築されています。

### 技術スタック
- **フレームワーク**: Spring Boot
- **ビュー**: Thymeleaf
- **ORM**: MyBatis
- **データベース**: H2 Database
- **ビルドツール**: Gradle

---

## レイヤーアーキテクチャ

```
┌─────────────────────────────┐
│   ユーザー（ブラウザ）        │
└──────────────┬──────────────┘
               ↕
┌─────────────────────────────┐
│   Controller層               │ ← リクエスト受付窓口
│   (TaskController)           │
└──────────────┬──────────────┘
               ↕
┌─────────────────────────────┐
│   Service層                  │ ← ビジネスロジック
│   (TaskService)              │
└──────────────┬──────────────┘
               ↕
┌─────────────────────────────┐
│   Repository層               │ ← データベース操作
│   (TaskRepository)           │
└──────────────┬──────────────┘
               ↕
┌─────────────────────────────┐
│   データベース (H2)          │
└─────────────────────────────┘
```

### 各層の役割

| 層 | 役割 | 例え |
|---|---|---|
| **Controller層** | ユーザーからのリクエストを受け取り、適切な処理を振り分ける | レストランのウェイター |
| **Service層** | ビジネスロジックを実行。複雑な処理やトランザクション管理を担当 | レストランのシェフ |
| **Repository層** | データベースとのやり取り。SQL実行を担当 | レストランの食材倉庫 |

---

## 主要ファイル解説

### 1. TodoApplication.java

**パス**: `src/main/java/com/example/todo/TodoApplication.java`

**役割**: アプリケーションのエントリーポイント（起動地点）

```java
@SpringBootApplication
public class TodoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
```

#### 解説
- `@SpringBootApplication`: Spring Bootアプリケーションとして起動するための必須アノテーション
- `main`メソッド: Javaプログラムの実行開始地点
- `SpringApplication.run()`: Spring Bootを起動し、すべてのコンポーネントを準備

#### 例え
映画館の「開場ボタン」のようなもの。このボタンを押すと、すべての設備（Controller、Service、Repositoryなど）が自動的に準備されます。

---

### 2. TaskStatus.java (Enum)

**パス**: `src/main/java/com/example/todo/service/task/TaskStatus.java`

**役割**: タスクのステータスを定義する列挙型

```java
public enum TaskStatus {
    TODO,    // 未着手
    DOING,   // 作業中
    DONE;    // 完了
}
```

#### 解説
- **enum（列挙型）**: 決まった値だけを持つことができる特別な型
- 文字列で管理するより安全（タイプミス防止）
- コンパイル時に型チェックされる

#### 使用例
```java
TaskStatus status = TaskStatus.TODO;
String statusName = status.name();  // "TODO"
```

#### メリット
- ✅ タイプミス防止（"TOOD"のようなミスが起きない）
- ✅ IDEの補完機能が使える
- ✅ 決まった値以外が入らない安全性

---

### 3. TaskEntity.java

**パス**: `src/main/java/com/example/todo/service/task/TaskEntity.java`

**役割**: データベースのtasksテーブルの1行分のデータを表現

```java
public record TaskEntity(
    Long id,              // タスクID
    String summary,       // 概要
    String description,   // 詳細説明
    TaskStatus status     // ステータス
) {}
```

#### 解説
- **record**: Java 14以降で使える、データを保持するためのシンプルなクラス
- 自動的にコンストラクタ、getter、equals、hashCode、toStringが生成される
- Service層とRepository層でデータを受け渡しする際に使用

#### 使用箇所
- Repository → Service へデータを返す
- Service → Repository へデータを渡す

#### データベーステーブルとの対応
```
tasksテーブル
┌────┬─────────┬─────────────┬────────┐
│ id │ summary │ description │ status │
├────┼─────────┼─────────────┼────────┤
│ 1  │ 買い物  │ 牛乳を買う  │ TODO   │
└────┴─────────┴─────────────┴────────┘
        ↓ TaskEntityに変換
TaskEntity(id=1, summary="買い物", description="牛乳を買う", status=TODO)
```

---

### 4. TaskSearchEntity.java

**パス**: `src/main/java/com/example/todo/service/task/TaskSearchEntity.java`

**役割**: 検索条件をService層からRepository層に渡すためのデータ

```java
public record TaskSearchEntity(
    String summary,           // 検索キーワード
    List<TaskStatus> status   // 検索対象のステータスリスト
) {}
```

#### データ変換の流れ
```
TaskSearchForm (Controller層)
    ↓ toDTO()
TaskSearchDTO (画面表示用)

TaskSearchForm (Controller層)
    ↓ new TaskSearchEntity()
TaskSearchEntity (Service/Repository層)
```

---

### 5. TaskController.java

**パス**: `src/main/java/com/example/todo/controller/task/TaskController.java`

**役割**: HTTPリクエストを受け取り、適切な処理を実行

```java
@Controller
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    // ...メソッド群
}
```

#### 重要なアノテーション

| アノテーション                      | 意味 |
|------------------------------|------|
| `@Controller`                | このクラスがコントローラーであることを示す |
| `@RequiredArgsConstructor`   | Lombokがfinalフィールドを引数に持つコンストラクタを自動生成 |
| `@RequestMapping("/tasks")`  | このコントローラーのベースURL |

---

#### 主要メソッド詳細

### ① タスク一覧表示 + 検索

```java
@GetMapping
public String list(TaskSearchForm searchForm, Model model) {
    // 1. ステータスリストを取得（nullの場合は空リスト）
    var statusEntityList = Optional.ofNullable(searchForm.status())
            .orElse(List.of());

    // 2. 検索条件をEntityに変換
    var searchEntity = new TaskSearchEntity(searchForm.summary(), statusEntityList);

    // 3. Serviceで検索実行 → DTOに変換
    var taskList = taskService.find(searchEntity)
            .stream()
            .map(TaskDTO::toDTO)
            .toList();

    // 4. 画面に渡すデータをModelに追加
    model.addAttribute("taskList", taskList);
    model.addAttribute("searchDTO", searchForm.toDTO());

    return "tasks/list";  // templates/tasks/list.htmlを表示
}
```

#### 処理フロー
```
1. ユーザーがアクセス
   GET /tasks?summary=買い物&status=TODO

2. Spring Bootが自動的にTaskSearchFormにデータを設定
   searchForm.summary() → "買い物"
   searchForm.status() → [TODO]

3. TaskSearchEntityに変換

4. taskService.find()でデータベース検索

5. 結果をTaskDTOに変換

6. Modelに追加して画面に渡す

7. templates/tasks/list.htmlが表示される
```

#### @GetMappingとは
- `@GetMapping`は`@RequestMapping(method = RequestMethod.GET)`の短縮形
- GETリクエストを受け付ける

---

### ② タスク詳細表示

```java
@GetMapping("/{id}")
public String showDetail(@PathVariable("id") long taskId, Model model) {
    var taskDTO = taskService.findById(taskId)
            .map(TaskDTO::toDTO)
            .orElseThrow(TaskNotFoundException::new);

    model.addAttribute("task", taskDTO);
    return "tasks/detail";
}
```

#### 解説
- `@PathVariable("id")`: URLの`{id}`部分を変数として取得
- `Optional.orElseThrow()`: データが見つからない場合は例外をスロー

#### 例
```
GET /tasks/5

@PathVariable("id") long taskId
↓
taskId = 5

taskService.findById(5)で検索
↓
見つかった → TaskDTOに変換して詳細画面表示
見つからない → TaskNotFoundExceptionをスロー
```

---

### ③ タスク作成フォーム表示

```java
@GetMapping("/creationForm")
public String showCreationForm(@ModelAttribute TaskForm form, Model model) {
    model.addAttribute("mode", "CREATE");
    return "tasks/form";
}
```

#### 解説
- `@ModelAttribute`: Formオブジェクトを自動的にModelに追加
- `mode = "CREATE"`: 画面側で作成モードか編集モードかを判定

---

### ④ タスク作成処理

```java
@PostMapping
public String create(@Validated TaskForm form, BindingResult bindingResult, Model model) {
    // バリデーションエラーがあったら作成フォームに戻る
    if (bindingResult.hasErrors()) {
        return showCreationForm(form, model);
    }

    // FormをEntityに変換して保存
    taskService.create(form.toEntity());

    return "redirect:/tasks";  // 一覧画面にリダイレクト
}
```

#### 処理フロー
```
1. ユーザーがフォーム送信
   POST /tasks
   summary=買い物&description=牛乳を買う&status=TODO

2. Spring Bootが自動的にTaskFormにデータを設定

3. @Validatedでバリデーション実行
   - summary が空でないか
   - summary が256文字以内か
   - status が正しい値か

4. エラーがあった場合
   → フォーム画面に戻る（エラーメッセージ表示）

5. エラーがない場合
   → form.toEntity()でTaskEntityに変換
   → taskService.create()でデータベースに保存
   → 一覧画面にリダイレクト
```

#### @Validatedとは
- バリデーション（入力検証）を実行するアノテーション
- TaskFormに定義された`@NotBlank`, `@Size`などのルールをチェック

#### BindingResultとは
- バリデーション結果を保持するオブジェクト
- `hasErrors()`: エラーがあるか判定
- エラーメッセージを画面に渡すことができる

---

### ⑤ タスク編集フォーム表示

```java
@GetMapping("/{id}/editForm")
public String showEditForm(@PathVariable("id") long id, Model model) {
    var form = taskService.findById(id)
            .map(TaskForm::fromEntity)
            .orElseThrow(TaskNotFoundException::new);

    model.addAttribute("taskForm", form);
    model.addAttribute("mode", "EDIT");
    return "tasks/form";
}
```

#### 処理フロー
```
1. GET /tasks/5/editForm

2. taskService.findById(5)でタスクを取得

3. TaskEntity → TaskFormに変換

4. mode = "EDIT"で編集モードを設定

5. tasks/form.htmlを表示（作成と同じフォーム）
```

---

### ⑥ タスク更新処理

```java
@PutMapping("{id}")
public String update(
        @PathVariable("id") long id,
        @Validated @ModelAttribute TaskForm form,
        BindingResult bindingResult,
        Model model
) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("mode", "EDIT");
        return "tasks/form";
    }

    var entity = form.toEntity(id);
    taskService.update(entity);
    return "redirect:/tasks/{id}";
}
```

#### 処理フロー
```
1. PUT /tasks/5

2. バリデーション実行

3. エラーがあれば編集フォームに戻る

4. エラーがなければ
   → form.toEntity(5)でIDを含むTaskEntityを作成
   → taskService.update()でデータベース更新
   → 詳細画面にリダイレクト
```

#### PUTメソッドの実装
HTMLフォームは標準でPUTをサポートしていないため、以下のように実装します：

```html
<form method="post" th:action="@{/tasks/{id}(id=${task.id})}">
    <input type="hidden" name="_method" value="PUT">
    <!-- フォームフィールド -->
</form>
```

Spring Bootが`_method`パラメータを検出し、PUTリクエストとして処理します。

---

### ⑦ タスク削除

```java
@DeleteMapping("{id}")
public String delete(@PathVariable("id") long id) {
    taskService.delete(id);
    return "redirect:/tasks";
}
```

#### 処理フロー
```
1. DELETE /tasks/5

2. taskService.delete(5)でタスクを削除

3. 一覧画面にリダイレクト
```

---

### 6. TaskService.java

**パス**: `src/main/java/com/example/todo/service/task/TaskService.java`

**役割**: ビジネスロジックを実行し、トランザクション管理を行う

```java
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    public List<TaskEntity> find(TaskSearchEntity searchEntity) {
        return taskRepository.select(searchEntity);
    }

    public Optional<TaskEntity> findById(long taskId) {
        return taskRepository.selectById(taskId);
    }

    @Transactional
    public void create(TaskEntity newEntity) {
        taskRepository.insert(newEntity);
    }

    @Transactional
    public void update(TaskEntity entity) {
        taskRepository.update(entity);
    }

    @Transactional
    public void delete(long id) {
        taskRepository.delete(id);
    }
}
```

#### 重要なアノテーション

| アノテーション | 意味 |
|--------------|------|
| `@Service` | このクラスがサービス層であることを示す。Springが自動的にBeanとして登録 |
| `@RequiredArgsConstructor` | finalフィールドを引数に持つコンストラクタを自動生成（依存性注入） |
| `@Transactional` | トランザクション管理。処理が途中で失敗したら全てロールバック |

#### @Transactionalの重要性

**例: トランザクションなしの場合**
```
1. ユーザー情報を更新
2. 注文情報を更新 ← ここでエラー発生！
→ ユーザー情報だけ更新されて、データの不整合が発生
```

**例: @Transactionalありの場合**
```
1. ユーザー情報を更新
2. 注文情報を更新 ← ここでエラー発生！
→ すべての処理がロールバック（元に戻る）
→ データの整合性が保たれる
```

#### Service層の責務
- Controller と Repository の橋渡し
- 複雑なビジネスロジックを記述
- トランザクション管理
- 複数のRepositoryを組み合わせた処理

---

### 7. TaskRepository.java

**パス**: `src/main/java/com/example/todo/repository/task/TaskRepository.java`

**役割**: データベース操作（CRUD）を実行

```java
@Mapper
public interface TaskRepository {

    @Select("""
        <script>
            SELECT id, summary, description, status
            FROM tasks
            <where>
               <if test='condition.summary != null and !condition.summary.isBlank()'>
                summary LIKE CONCAT('%' , #{condition.summary}, '%')
               </if>
               <if test='condition.status != null and !condition.status.isEmpty()'>
                AND status IN (
                   <foreach item='item' index='index' collection='condition.status' separator=','>
                    #{item}
                   </foreach>
                   )
                </if>
            </where>
        </script>
        """)
    List<TaskEntity> select(@Param("condition") TaskSearchEntity condition);

    @Select("SELECT id, summary, description, status FROM tasks WHERE id = #{taskId}")
    Optional<TaskEntity> selectById(@Param("taskId") long taskId);

    @Insert("""
        INSERT INTO tasks(summary, description, status)
        VALUES (#{task.summary}, #{task.description}, #{task.task})
        """)
    void insert(@Param("task") TaskEntity newEntity);

    @Update("""
        UPDATE tasks
        SET
            summary = #{task.summary},
            description = #{task.description},
            status = #{task.status}
        WHERE
            id = #{task.id}
        """)
    void update(@Param("task") TaskEntity entity);

    @Delete("DELETE FROM tasks WHERE id = #{taskId}")
    void delete(@Param("taskId") long id);
}
```

#### MyBatisアノテーション

| アノテーション | SQL操作 |
|--------------|---------|
| `@Select` | SELECT（データ取得） |
| `@Insert` | INSERT（データ挿入） |
| `@Update` | UPDATE（データ更新） |
| `@Delete` | DELETE（データ削除） |
| `@Param` | SQLのパラメータ名を指定 |

#### 動的SQL解説

```java
@Select("""
    <script>
        SELECT id, summary, description, status
        FROM tasks
        <where>
           <if test='condition.summary != null and !condition.summary.isBlank()'>
            summary LIKE CONCAT('%' , #{condition.summary}, '%')
           </if>
           <if test='condition.status != null and !condition.status.isEmpty()'>
            AND status IN (
               <foreach item='item' index='index' collection='condition.status' separator=','>
                #{item}
               </foreach>
               )
            </if>
        </where>
    </script>
    """)
```

##### 動的SQLタグ

| タグ | 役割 |
|-----|------|
| `<script>` | 動的SQLを使うための宣言 |
| `<where>` | 条件がある場合のみWHERE句を追加。不要なANDも自動削除 |
| `<if test='...'>` | 条件が真の場合のみSQL内に含める |
| `<foreach>` | コレクションをループしてSQLを生成 |

##### 実行例

**ケース1: summaryのみ指定**
```java
// 入力
summary = "買い物"
status = null

// 生成されるSQL
SELECT id, summary, description, status
FROM tasks
WHERE summary LIKE '%買い物%'
```

**ケース2: statusのみ指定**
```java
// 入力
summary = null
status = [TODO, DOING]

// 生成されるSQL
SELECT id, summary, description, status
FROM tasks
WHERE status IN ('TODO', 'DOING')
```

**ケース3: 両方指定**
```java
// 入力
summary = "買い物"
status = [TODO]

// 生成されるSQL
SELECT id, summary, description, status
FROM tasks
WHERE summary LIKE '%買い物%' AND status IN ('TODO')
```

**ケース4: どちらも未指定**
```java
// 入力
summary = null
status = null

// 生成されるSQL
SELECT id, summary, description, status
FROM tasks
```

---

### 8. TaskForm.java

**パス**: `src/main/java/com/example/todo/controller/task/TaskForm.java`

**役割**: フォーム入力を受け取り、バリデーションを行う

```java
public record TaskForm(
    @NotBlank
    @Size(max = 256, message = "256文字以内で入力してください")
    String summary,

    String description,

    @NotBlank
    @Pattern(regexp = "TODO|DOING|DONE", message = "TODO, DOING, DONE のいずれかを選択してください")
    String status
) {
    // EntityをFormに変換（編集時に使用）
    public static TaskForm fromEntity(TaskEntity taskEntity) {
        return new TaskForm(
                taskEntity.summary(),
                taskEntity.description(),
                taskEntity.status().name()
        );
    }

    // FormをEntityに変換（新規作成用、IDなし）
    public TaskEntity toEntity() {
        return new TaskEntity(null, summary(), description(), TaskStatus.valueOf(status()));
    }

    // FormをEntityに変換（更新用、IDあり）
    public TaskEntity toEntity(long id) {
        return new TaskEntity(id, summary(), description(), TaskStatus.valueOf(status()));
    }
}
```

#### バリデーションアノテーション

| アノテーション | 意味 | 使用例 |
|--------------|------|--------|
| `@NotBlank` | null、空文字、空白のみを許可しない | 必須入力項目 |
| `@Size(max = 256)` | 最大文字数を制限 | 256文字以内 |
| `@Pattern(regexp = "...")` | 正規表現でパターンマッチング | "TODO\|DOING\|DONE" |

#### メソッド解説

**fromEntity()**: EntityをFormに変換
```java
// 使用例: 編集画面でデータベースのデータをフォームに表示
TaskEntity entity = taskService.findById(5);
TaskForm form = TaskForm.fromEntity(entity);
```

**toEntity()**: FormをEntityに変換（新規作成）
```java
// 使用例: フォーム送信後、データベースに保存
TaskForm form = new TaskForm("買い物", "牛乳", "TODO");
TaskEntity entity = form.toEntity();  // id = null
taskService.create(entity);
```

**toEntity(long id)**: FormをEntityに変換（更新）
```java
// 使用例: 編集フォーム送信後、データベースを更新
TaskForm form = new TaskForm("買い物", "牛乳と卵", "DOING");
TaskEntity entity = form.toEntity(5);  // id = 5
taskService.update(entity);
```

---

### 9. TaskDTO.java

**パス**: `src/main/java/com/example/todo/controller/task/TaskDTO.java`

**役割**: 画面表示用のデータ形式

```java
public record TaskDTO(
    long id,
    String summary,
    String description,
    String status
) {
    public static TaskDTO toDTO(TaskEntity entity) {
        return new TaskDTO(
                entity.id(),
                entity.summary(),
                entity.description(),
                entity.status().name()  // enumを文字列に変換
        );
    }
}
```

#### EntityとDTOの違い

| 項目 | TaskEntity | TaskDTO |
|-----|-----------|---------|
| 使用場所 | Service層、Repository層 | Controller層、View（HTML） |
| statusの型 | `TaskStatus`（enum） | `String` |
| 目的 | データベースとのやり取り | 画面表示 |

#### なぜDTOが必要か？

**理由1: 画面で扱いやすい形式に変換**
```java
// Entity: status = TaskStatus.TODO (enum)
// DTO: status = "TODO" (String) ← HTMLで扱いやすい
```

**理由2: 必要なデータだけを公開**
```java
// Entityには内部情報が含まれることがある
// DTOで画面に必要なデータだけをフィルタリング
```

**理由3: セキュリティ**
```java
// パスワードなどの機密情報を除外できる
```

---

### 10. TaskSearchForm.java

**パス**: `src/main/java/com/example/todo/controller/task/TaskSearchForm.java`

**役割**: 検索フォームからの入力を受け取る

```java
public record TaskSearchForm(
    String summary,
    List<TaskStatus> status
) {
    public TaskSearchDTO toDTO() {
        return new TaskSearchDTO(summary(), status());
    }
}
```

#### 使用フロー
```
1. ユーザーが検索フォーム入力
   summary: "買い物"
   status: [TODO, DOING]

2. Spring BootがTaskSearchFormに自動変換

3. Controllerで受け取る

4. TaskSearchDTOに変換（画面に戻す用）

5. TaskSearchEntityに変換（検索実行用）
```

---

### 11. TaskSearchDTO.java

**パス**: `src/main/java/com/example/todo/controller/task/TaskSearchDTO.java`

**役割**: 検索条件を画面に戻す際に使用

```java
public record TaskSearchDTO(
    String summary,
    List<TaskStatus> statusList
) {
    public boolean isChecked(String status) {
        return Optional.ofNullable(statusList)
                .map(list -> list.stream()
                        .anyMatch(taskStatus -> taskStatus.name().equals(status)))
                .orElse(false);
    }
}
```

#### isChecked()メソッド詳細

**目的**: チェックボックスの状態を復元

```java
// HTML側での使用
<input type="checkbox" value="TODO" th:checked="*{isChecked('TODO')}">
```

**動作フロー**
```
1. statusList = [TODO, DOING]の場合

2. isChecked('TODO')が呼ばれる

3. statusListの各要素のname()と比較
   - TaskStatus.TODO → name()で"TODO" → "TODO".equals("TODO") → true

4. trueを返す → チェックボックスにチェックが入る
```

**null安全**
```java
Optional.ofNullable(statusList)
    .map(...)
    .orElse(false);

// statusListがnullの場合 → falseを返す（エラーにならない）
```

---

## データの流れ

### タスク一覧表示 + 検索の流れ

```
┌─────────────────────────────────────┐
│ 1. ユーザーがブラウザで検索         │
│    GET /tasks?summary=買い物&status=TODO │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 2. TaskController.list()            │
│    - TaskSearchForm に自動セット    │
│    - TaskSearchEntity に変換        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 3. TaskService.find()               │
│    - 検索条件を渡す                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 4. TaskRepository.select()          │
│    - 動的SQLを生成                  │
│    - データベース検索実行           │
│    - TaskEntity のリストを返す      │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 5. Service → Controller             │
│    - TaskEntity を TaskDTO に変換   │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 6. Controller → View (Thymeleaf)    │
│    - Model に taskList 追加         │
│    - Model に searchDTO 追加        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 7. ブラウザに検索結果を表示         │
│    - 検索条件も保持                 │
└─────────────────────────────────────┘
```

---

### タスク作成の流れ

```
┌─────────────────────────────────────┐
│ 1. ユーザーがフォーム送信           │
│    POST /tasks                      │
│    summary=買い物&description=牛乳&status=TODO │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 2. TaskController.create()          │
│    - TaskForm に自動セット          │
│    - @Validated でバリデーション    │
│    - エラーあり → フォームに戻る    │
│    - エラーなし → 次へ              │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 3. TaskForm → TaskEntity 変換       │
│    form.toEntity()                  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 4. TaskService.create()             │
│    - @Transactional でトランザクション開始 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 5. TaskRepository.insert()          │
│    - INSERT SQL実行                 │
│    - データベースに保存             │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 6. Controller                       │
│    - "redirect:/tasks" を返す       │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 7. ブラウザが /tasks にリダイレクト │
│    - 一覧画面が表示される           │
└─────────────────────────────────────┘
```

---

### タスク更新の流れ

```
┌─────────────────────────────────────┐
│ 1. 編集フォーム表示                 │
│    GET /tasks/5/editForm            │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 2. TaskController.showEditForm()    │
│    - taskService.findById(5)        │
│    - TaskEntity → TaskForm 変換     │
│    - mode = "EDIT"                  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 3. フォーム画面表示                 │
│    - 既存データが入力済み           │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 4. ユーザーが編集して送信           │
│    PUT /tasks/5                     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 5. TaskController.update()          │
│    - バリデーション                 │
│    - form.toEntity(5) でID含む変換  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 6. TaskService.update()             │
│    - @Transactional                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 7. TaskRepository.update()          │
│    - UPDATE SQL実行                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 8. 詳細画面にリダイレクト           │
│    redirect:/tasks/5                │
└─────────────────────────────────────┘
```

---

### タスク削除の流れ

```
┌─────────────────────────────────────┐
│ 1. ユーザーが削除ボタンクリック     │
│    DELETE /tasks/5                  │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 2. TaskController.delete()          │
│    - @PathVariable で id=5 取得     │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 3. TaskService.delete(5)            │
│    - @Transactional                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 4. TaskRepository.delete(5)         │
│    - DELETE SQL実行                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│ 5. 一覧画面にリダイレクト           │
│    redirect:/tasks                  │
└─────────────────────────────────────┘
```

---

## まとめ

### ファイル役割一覧表

| ファイル | 層 | 役割 | 例え |
|---------|-----|------|------|
| **TodoApplication** | - | アプリケーション起動 | 開場ボタン |
| **TaskController** | Controller | リクエスト受付、レスポンス返却 | レストランのウェイター |
| **TaskService** | Service | ビジネスロジック、トランザクション管理 | レストランのシェフ |
| **TaskRepository** | Repository | データベース操作（SQL実行） | 食材倉庫の管理者 |
| **TaskEntity** | Model | データベースのデータ表現 | データベースの1行 |
| **TaskDTO** | Model | 画面表示用データ | ユーザーに見せる料理 |
| **TaskForm** | Model | フォーム入力データ（バリデーション付き） | 注文票（記入ルールあり） |
| **TaskSearchForm** | Model | 検索フォーム入力データ | 検索条件の注文票 |
| **TaskSearchDTO** | Model | 検索条件を画面に戻すデータ | 検索条件の控え |
| **TaskSearchEntity** | Model | 検索条件をRepositoryに渡すデータ | 倉庫への検索依頼書 |
| **TaskStatus** | Enum | タスクのステータス定義 | 信号の色（青・黄・赤） |

---

### レイヤー間のデータ変換

```
┌────────────────────────────────────────┐
│           Controller層                 │
│  TaskForm, TaskDTO, TaskSearchForm     │
│  TaskSearchDTO                         │
└───────────────┬────────────────────────┘
                ↓ 変換
┌────────────────────────────────────────┐
│           Service層                    │
│  TaskEntity, TaskSearchEntity          │
└───────────────┬────────────────────────┘
                ↓ そのまま
┌────────────────────────────────────────┐
│           Repository層                 │
│  TaskEntity, TaskSearchEntity          │
└───────────────┬────────────────────────┘
                ↓
┌────────────────────────────────────────┐
│           Database                     │
│  tasks テーブル                        │
└────────────────────────────────────────┘
```

---

### 重要な設計原則

#### 1. 単一責任の原則
各クラスは1つの責任だけを持つ
- Controller: リクエスト・レスポンス処理
- Service: ビジネスロジック
- Repository: データベース操作

#### 2. 依存性注入（DI）
- `@RequiredArgsConstructor` でコンストラクタ自動生成
- Spring がインスタンスを自動的に注入
- テストしやすい、疎結合な設計

#### 3. レイヤーアーキテクチャ
- 各層が独立
- 変更の影響範囲を最小化
- テストしやすい

#### 4. DTO パターン
- 層ごとに適切なデータ形式を使う
- セキュリティ向上
- 画面表示に最適化

---

### Spring Boot の便利機能

| 機能 | 説明 | 例 |
|-----|------|-----|
| **自動バインディング** | リクエストパラメータを自動的にオブジェクトに変換 | `?summary=買い物` → `TaskSearchForm` |
| **バリデーション** | `@Validated` で入力チェック自動化 | `@NotBlank`, `@Size` |
| **依存性注入** | `@RequiredArgsConstructor` で自動注入 | `TaskService` を自動注入 |
| **トランザクション** | `@Transactional` で自動管理 | エラー時に自動ロールバック |
| **Thymeleaf統合** | Model のデータを自動的にテンプレートに渡す | `model.addAttribute()` |

---

### MyBatis の便利機能

| 機能 | 説明 |
|-----|------|
| **動的SQL** | 条件によってSQLを動的に生成 |
| **自動マッピング** | 結果セットを自動的にオブジェクトに変換 |
| **アノテーションベース** | XMLなしでSQLを定義可能 |
| **型安全** | コンパイル時にエラーを検出 |

---

### ベストプラクティス

#### ✅ やるべきこと
- `@Transactional` を更新系メソッドに付ける
- バリデーションを必ず実装
- DTOで画面と内部データを分離
- 例外処理を適切に行う
- null安全なコードを書く（`Optional`活用）

#### ❌ やってはいけないこと
- Controller から直接 Repository を呼ぶ
- Service層をスキップする
- バリデーションを省略
- SQL インジェクション対策を怠る
- トランザクション管理を忘れる

---

### さらに学ぶべきトピック

1. **Spring Security** - 認証・認可
2. **例外ハンドリング** - `@ControllerAdvice`
3. **テスト** - JUnit, Mockito
4. **ロギング** - Logback, SLF4J
5. **REST API** - `@RestController`, JSON
6. **ページネーション** - 大量データの処理
7. **キャッシング** - `@Cacheable`
8. **非同期処理** - `@Async`

---

## 用語集

| 用語 | 説明 |
|-----|------|
| **DTO** | Data Transfer Object。データを運ぶための入れ物 |
| **Entity** | データベースのテーブルに対応するオブジェクト |
| **Record** | Java 14以降のデータクラス。不変オブジェクト |
| **Enum** | 列挙型。決まった値だけを持つ型 |
| **Annotation** | `@`で始まる、クラスやメソッドに付ける目印 |
| **DI** | Dependency Injection。依存性注入 |
| **ORM** | Object-Relational Mapping。オブジェクトとDBのマッピング |
| **Transaction** | 一連の処理をまとめて成功/失敗させる仕組み |
| **Validation** | 入力値のチェック |
| **Binding** | リクエストパラメータをオブジェクトに変換 |

---

**作成日**: 2025年
**対象**: Spring Boot 初心者〜中級者
**バージョン**: Spring Boot 3.x, Java 17+
