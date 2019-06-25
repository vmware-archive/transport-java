// /*
//  * @copyright Copyright VMware, Inc. All rights reserved. VMware Confidential.
//  * @license For licensing, see LICENSE.md.
//  */
//
// import { FactoryProvider } from '@angular/core';
// import { OAuth2Client, OAuth2ClientConfiguration } from '@vmw/csp-oauth2';
//
//
// const OAUTH_CONFIG: OAuth2ClientConfiguration = {
//     clientId: 'ngx-app-client',
//     clientSecret: 'not_a_secret',
//     scopes: [
//         'csp:org_member',
//         'csp:org_owner',
//         'csp:platform_operator',
//         'csp:support_user',
//         'customer_number',
//         'external/5033276b-d0a0-4a0b-bd70-3f2c423ff7cf/ngx_eng:admin',
//         'external/5033276b-d0a0-4a0b-bd70-3f2c423ff7cf/ngx_eng:user',
//         'openid'
//     ],
//     redirectUri: `${window.location.protocol}//${window.location.host}`,
//     authorizationUrl: 'https://console-stg.cloud.vmware.com/csp/gateway/discovery',
//     tokenUrl: 'https://console-stg.cloud.vmware.com/csp/gateway/am/api/auth/authorize',
//     logoutUrl: 'https://console-stg.cloud.vmware.com/csp/gateway/am/api/auth/logout'
// };
//
// /**
//  * OAuth2 client factory.
//  */
// export function clientFactory(): OAuth2Client {
//     return new OAuth2Client(OAUTH_CONFIG);
// }
//
// /**
//  * Inject the OAuth2 Client.
//  */
// export const CLIENT_PROVIDER: FactoryProvider = {
//     provide: OAuth2Client,
//     useFactory: clientFactory
// };
//
