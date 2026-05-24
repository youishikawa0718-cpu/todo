# TODOタスク管理アプリケーション

Spring Bootで構築されたシンプルなTODOタスク管理Webアプリケーションです。
https://todo-app-wbt7.onrender.com

## 背景・目的

JavaとSpring Bootを学習する目的で個人開発した、Webアプリケーション制作の練習プロジェクトです。
書籍やオンライン教材で学んだ知識を、実際に手を動かしながら定着させることを目的としています。

題材としてTODO管理アプリを選んだ理由は、CRUDの一通りの操作を含みつつ、ステータス遷移や検索条件など、
業務アプリでよく登場する要素をコンパクトに体験できるためです。

具体的に学んだこと:

- Spring MVC（Controller → Service → Repository）のレイヤー構成
- Thymeleafによるサーバーサイドレンダリング
- MyBatisによるSQLマッピングと動的SQL
- Form / DTO / Entity を分離する設計の意図
- Renderを使った本番デプロイとPostgreSQL接続
- JUnit 5を用いた単体テストの書き方（AAAパターン、アサーションの使い分け）

## 機能

- タスクのCRUD操作（作成・閲覧・更新・削除）
- タスクのステータス管理（TODO/DOING/DONE）
- タスクの検索機能（概要、ステータス）
- Bootstrapによるレスポンシブデザイン

## 技術スタック

- **フレームワーク**: Spring Boot 3.1.2
- **言語**: Java 17
- **ビルドツール**: Gradle
- **テンプレートエンジン**: Thymeleaf
- **O/Rマッパー**: MyBatis
- **データベース**:
  - 開発環境: H2 Database (インメモリ)
  - 本番環境: PostgreSQL
- **CSSフレームワーク**: Bootstrap 5.2.3

## ローカル開発環境のセットアップ

### 前提条件

- Java 17以上
- Gradle 8.x（またはGradle Wrapperを使用）

### 実行方法

1. リポジトリをクローン:
```bash
git clone <your-repository-url>
cd spring-boot-introduction-start-here
```

2. アプリケーションを起動:
```bash
./gradlew bootRun
```

3. ブラウザでアクセス:
```
http://localhost:8080
```

### テスト実行

```bash
./gradlew test
```

### ビルド

```bash
./gradlew clean build
```

## Renderへのデプロイ

### 前提条件

- GitHubアカウント
- Renderアカウント（無料）

### デプロイ手順

1. **GitHubにプッシュ**:
   ```bash
   git add .
   git commit -m "Prepare for Render deployment"
   git push origin master
   ```

2. **Renderでデータベースを作成**:
   - [Render Dashboard](https://dashboard.render.com/)にログイン
   - 「New +」→「PostgreSQL」を選択
   - 設定:
     - Name: `todo-db`
     - Database: `todo`
     - User: `todo_user`
     - Region: お好みのリージョン（Singapore推奨）
     - Instance Type: Free
   - 「Create Database」をクリック
   - **重要**: 作成後、Database URLをコピーしておく

3. **RenderでWebサービスを作成**:
   - 「New +」→「Web Service」を選択
   - GitHubリポジトリを接続
   - 設定:
     - Name: `todo-app`
     - Runtime: `Java`
     - Build Command: `./gradlew clean build -x test`
     - Start Command: `java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/*.jar`
     - Instance Type: Free

4. **環境変数を設定**:
   - 「Environment」タブで以下を追加:
     - `DATABASE_URL`: 先ほどコピーしたPostgreSQL Database URL
     - `SPRING_PROFILES_ACTIVE`: `prod`
     - `JAVA_TOOL_OPTIONS`: `-Xmx512m`

5. **デプロイ**:
   - 「Create Web Service」をクリック
   - 自動的にビルドとデプロイが開始されます
   - デプロイ完了後、提供されたURLにアクセス

### render.yamlを使った自動デプロイ

リポジトリに`render.yaml`が含まれているため、以下の手順でより簡単にデプロイできます：

1. Renderダッシュボードで「New +」→「Blueprint」を選択
2. GitHubリポジトリを接続
3. `render.yaml`が自動的に検出されます
4. 「Apply」をクリック
5. データベースとWebサービスが自動的に作成されます

## プロジェクト構造

```
src/
├── main/
│   ├── java/
│   │   └── com/example/todo/
│   │       ├── controller/     # コントローラー層
│   │       ├── service/        # ビジネスロジック層
│   │       ├── repository/     # データアクセス層
│   │       └── entity/         # エンティティ
│   └── resources/
│       ├── templates/          # Thymeleafテンプレート
│       ├── static/             # 静的リソース
│       ├── schema.sql          # H2用DDL
│       ├── schema-postgres.sql # PostgreSQL用DDL
│       ├── data.sql            # 初期データ
│       ├── application.properties           # 開発環境設定
│       └── application-prod.properties      # 本番環境設定
└── test/                       # テストコード
```

## 苦労した点・学んだこと

### 1. アノテーションの概念理解

Spring Bootは多数のアノテーションを使って動作するフレームワークで、最初は
「なぜクラスに`@Service`を1行付けただけでSpringが勝手にインスタンスを作ってくれるのか」
「`@Transactional`を付けるとなぜトランザクションが効くのか」といった**「裏で何が起きているのか」**
が掴めず苦労しました。

具体的にハマった点:

- アノテーションは**それ単体では何もせず、フレームワーク側が読み取って動く**という二重構造の理解
- DI（依存性注入）の仕組み: `@Service` `@Controller` `@Mapper` でBean登録され、
  コンストラクタ経由で自動的に渡される（Lombokの`@RequiredArgsConstructor`との組み合わせ）
- 似たアノテーションの使い分け:
  - `@PathVariable`（URLパスから受け取る）
  - `@RequestParam`（クエリパラメータから受け取る）
  - `@ModelAttribute`（フォームからオブジェクトとして受け取る）
- `@Transactional`はAOPプロキシで動くため**同一クラス内のメソッド呼び出しでは効かない**こと
- `@Validated`と`@Valid`の違い、バリデーションがどのタイミングで走るか

「動けばいい」で進めるとアノテーションを呪文のように貼ってしまいがちだったので、
**なぜそのアノテーションが必要か・付けるとフレームワークが何をしてくれるのか**を意識して
学ぶようにしました。リフレクションやAOPといった、アノテーションを支える基礎技術にも興味を持つきっかけになりました。

### 2. テストコードを初めて書いたこと

これまで書籍やチュートリアルでテストを「動かす」経験はあったものの、自分で1から書いたのは初めてでした。
本プロジェクトで以下の点を実践しながら学びました:

- **AAAパターン**（Arrange / Act / Assert）でテストを構造化する
- 1つのテストメソッドで1つのことだけを検証する
- `assertEquals`と`assertNull`を意図に合わせて使い分ける
- テストメソッド名を「対象_条件_期待結果」の形式で日本語で書く

特に印象に残ったのは、`assertEquals(null, ...)`を書いたときに「どのオーバーロードが呼ばれるのか」を
調べた経験です。`null`はプリミティブに変換できないため必ず`Object`版が選ばれることや、
そもそも`assertNull`の方が意図が伝わることなど、「動けばいい」で終わらせない姿勢を意識するようになりました。

### 3. その他に学んだ設計上の気付き

- **Form / DTO / Entityを層ごとに分離**することで、画面の都合が内部ドメインに染み出さない
- **`record`の活用**でイミュータブルかつ短くデータクラスを書ける
- **`enum`でステータスを型付け**することで、不正な状態をコンパイル時に弾ける
- Serviceの`@Transactional`が「1ユースケース = 1トランザクション」の境界になる

## ライセンス

このプロジェクトは学習目的で作成されています。

## 詳細ドキュメント

- [要件定義書](REQUIREMENTS_SPECIFICATION.md)
- [実装解説ドキュメント](TODO_APP_DOCUMENTATION.md)
