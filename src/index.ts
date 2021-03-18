import { registerPlugin } from '@capacitor/core';

import type { NativeDownloaderPlugin } from './definitions';

const NativeDownloader = registerPlugin<NativeDownloaderPlugin>(
  'NativeDownloader',
);

export * from './definitions';
export { NativeDownloader };
