// ダウンロードの追加
export interface IAddDownloadRequest {
  id: string;
  url: string;
  size: number;
  filePath: string;
  fileName: string;
  displayName: string; // 通知時に表示する名前
  headers?: {
    authorization: string;
  };
}
export interface IAddDownloadResponse {
  id: string;
  fileName: string; // music.mp3
  absolutePath: string;
}

// スタート
export interface IStartDownloadRequest {
  id: string;
}
export interface IStartDownloadResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

// 一時停止
export interface IPauseDownloadRequest {
  id: string;
}
export interface IPauseDownloadResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

// 再開
export interface IResumeDownloadRequest {
  id: string;
}
export interface IResumeDownloadResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

// キャンセル
export interface ICancelDownloadRequest {
  id: string;
}
export interface ICancelDownloadResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

export interface NativeDownloaderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  add(options: { params: IAddDownloadRequest }): Promise<IAddDownloadResponse>;
  start(options: {
    params: IStartDownloadRequest;
  }): Promise<IStartDownloadResponse>;

  pause(options: {
    params: IPauseDownloadRequest;
  }): Promise<IPauseDownloadResponse>;

  resume(options: {
    params: IResumeDownloadRequest;
  }): Promise<IResumeDownloadResponse>;

  cancel(options: {
    params: ICancelDownloadRequest;
  }): Promise<ICancelDownloadResponse>;
}

declare module '@capacitor/core' {
  interface PluginRegistry {
    NativeDownloaderPlugin: NativeDownloaderPlugin;
  }
}
