// ダウンロードの追加
export interface IAddDownloadRequest {
  id: string;
  url: string;
  size: number;
  filePath: string;
  fileName: string;
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
export interface IPauseRequest {
  id: string;
}
export interface IPauseResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

// 再開
export interface IResumeRequest {
  id: string;
}
export interface IResumeResponse {
  id: string;
  filename: string;
  absolutePath: string;
}

// ストップ
export interface IStopRequest {
  id: string;
}
export interface IStopResponse {
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
}
