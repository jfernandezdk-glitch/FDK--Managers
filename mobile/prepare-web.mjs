import {mkdir,copyFile} from 'node:fs/promises';
await mkdir('www',{recursive:true});
await copyFile('../panel-r31.html','www/index.html');
await copyFile('../icon-192.png','www/icon-192.png');
await copyFile('../manifest-r31.webmanifest','www/manifest-r31.webmanifest');
await copyFile('../panel-r31.html','www/panel-r31.html');
