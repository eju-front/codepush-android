# Eju CodePush Android

## 安装方法

将 `library` 目录下的 `libEjuCodePush-release.jar` 放在应用的 `libs` 目录下

## 使用指南

1. 自定义 Application, 在 `onCreate()` 方法中初始化 EjuCodePush 对象

```java
  public class App extends Application {

      private EjuCodePush codePush;

      @Override
      public void onCreate() {
          super.onCreate();

          // 由于需要进行替换，所以该 html 路径需要确保可读写(所以不能是 assets 目录)
          String htmlDirectory = "h5 资源路径";

          codePush = new EjuCodePush();

          // 配置 EjuCodePush 对象
          Option option = new Option();

          // 可选，设置应用名，默认为当前应用的包名
          option.appName = "demo";

          // 二选一，以下两种选择互斥
          // 第一种，设置 Base Url
          option.baseUrl = "http://172.29.108.138:10086/app";
          // 第二种，分别设置 checkVersionUrl 和 downloadUrl
          // 设置检查应用版本的 Url
          // option.checkVersionUrl = "xxx";
          // 设置下载新版本资源的 Url
          // option.downloadUrl = "xxx";

          // 必选，设置本地 H5 资源路径
          option.htmlDirectory = htmlDirectory;
          codePush.initialize(this, option);
      }

      public EjuCodePush getCodePush() {
          return codePush;
      }
  }
```

2. 在需要进行更新的画面(通常为包含 webview 的画面)调用以下代码进行同步操作

```java
  EjuCodePush codePush = ((App)getApplication()).getCodePush();
  codePush.syncInBackground(this,
           "有新版本的应用",
           "确定",
           "下次提醒",
           "更新",
           "下载中。。。",
           new SyncCallback() {
      @Override
      public void onSuccess(boolean isUpdate, boolean needReload) {
          if (needReload) {
              webView.reload();
          }
      }

      @Override
      public void onError(EjuCodePushException e) {
          e.printStackTrace();
      }
  });
```

 	以上 `syncInBackground()` 方法的参数依次为 `Activity`, `有新版本的提示信息`，`确定按钮文字`,`取消按钮文字`，`下载对话框标题`，`下载对话框信息`。

​	EjuCodePush 支持下次更新和强制更新两种方式，只有强制更新时以上回调才会被执行。
`onSuccess()` 回调的两个参数,其中 `isUpdate` 表示此次是否进行了更新操作,`needReload` 表示 h5 资源是否进行了更新