import { WebPlugin } from '@capacitor/core';

import type { NativeDownloaderPlugin } from './definitions';

export class NativeDownloaderWeb
  extends WebPlugin
  implements NativeDownloaderPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
