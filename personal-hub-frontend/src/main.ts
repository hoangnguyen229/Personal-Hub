import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';

import { registerLicense } from '@syncfusion/ej2-base';

if (environment.production) {
  enableProdMode();
}

registerLicense('ORg4AjUWIQA/Gnt2XFhhQlJHfVpdX2BWfFN0QHNYdV50flZOcC0sT3RfQFhjTXxQdkdnX31ZeHRcR2teWA==');

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
