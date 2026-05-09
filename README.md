# TODOタスク管理アプリケーション

Spring Bootで構築されたシンプルなTODOタスク管理Webアプリケーションです。
https://todo-app-wbt7.onrender.com）

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

## ライセンス

このプロジェクトは学習目的で作成されています。

## 詳細ドキュメント

- [要件定義書](REQUIREMENTS_SPECIFICATION.md)
- [実装解説ドキュメント](TODO_APP_DOCUMENTATION.md)
