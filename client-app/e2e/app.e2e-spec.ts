import { ClientAppPage } from './app.po';

describe('client-app App', () => {
  let page: ClientAppPage;

  beforeEach(() => {
    page = new ClientAppPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!');
  });
});
