import { registerPlugin } from '@capacitor/core';

import type { NativeDownloaderPlugin } from './definitions';

const NativeDownloader = registerPlugin<NativeDownloaderPlugin>(
  'NativeDownloader',
  {
    web: () => import('./web').then(m => new m.NativeDownloaderWeb()),
  },
);

export * from './definitions';
export { NativeDownloader };
