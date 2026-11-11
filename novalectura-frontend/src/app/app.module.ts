import { NgModule } from '@angular/core';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';
import { AuthModule } from './auth/auth.module';
import { CatalogueModule } from './catalogue/catalogue.module'; 
import { ComponentsModule } from './components/components.module';
import { ProductManagementsModule } from './product-managements/product-managements.module';
import { ShoppingCarModule } from './shopping-car/shopping-car.module';
import { LocalStorageService } from './services/local-storage.service';
import { AuthService } from './services/auth.service';
import { JwtInterceptor } from './services/jwt.interceptor';


// Redux

import { NgxsModule } from '@ngxs/store';

import { CatalogeState } from './redux/catalogue/catalogue.redux';

@NgModule({
  imports: [
    HttpClientModule,
    AppRoutingModule,
    AuthModule,
    CatalogueModule,
    ComponentsModule,
    ProductManagementsModule,
    ShoppingCarModule,

    // Redux
    NgxsModule.forRoot([
      CatalogeState
    ])
  ],
  providers: [
    LocalStorageService,
    AuthService,
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true }
  ]
})
export class AppModule { }
