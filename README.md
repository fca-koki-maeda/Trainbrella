# Trainbrella
2年　WEBプログラミングⅡ　アプリ開発課題　（2026進級制作展展示作品）

## 環境変数の設定
提出用のソースでは機密情報を含めないため、起動前に環境変数で値を渡します。  
PowerShell などのシェル内で以下を実行してください。  
Gmail は通常のパスワードではなく「アプリパスワード」を使用してください。

```powershell
# OpenWeatherMap
$env:OPENWEATHER_API_KEY="<あなたのAPIキー>"

# Gmail
$env:MAIL_USERNAME="<あなたのGmailアドレス>"
$env:MAIL_PASSWORD="<あなたのGmailアプリパスワード>"
$env:MAIL_FROM="<差出人メールアドレス>"

# SMTP設定を変更する場合のみ
$env:MAIL_SMTP_HOST="smtp.gmail.com"
$env:MAIL_SMTP_PORT="587"
```

## 起動方法
mvn clean spring-boot:run


## 補足
これらの環境変数は **PowerShell セッション内だけ** 有効です。毎回起動前に設定してください。
