import { BrowserCacheLocation, Configuration } from '@azure/msal-browser';

export const TENANT_ID = 'd39637a6-cb63-4031-b19a-d1c1a17eb202';
export const SPA_CLIENT_ID = 'afbf4c86-b644-41d6-b46d-74aba4d0bf2c';
export const API_CLIENT_ID = 'da4ade51-40ca-4222-8838-306a092a85c1';
export const API_BASE_URL = 'https://ew6ntqlmih.execute-api.us-east-1.amazonaws.com';

export const apiScopes = [`api://${API_CLIENT_ID}/access_as_user`];

export const msalConfig: Configuration = {
  auth: {
    clientId: SPA_CLIENT_ID,
    authority: `https://login.microsoftonline.com/common`,
    redirectUri: 'http://localhost:4200',
    postLogoutRedirectUri: 'http://localhost:4200',
  },
  cache: {
    cacheLocation: BrowserCacheLocation.LocalStorage,
  },
};