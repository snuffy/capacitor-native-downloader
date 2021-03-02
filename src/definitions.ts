export interface NativeDownloaderPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
