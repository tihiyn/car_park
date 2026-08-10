describe('Авторизация', () => {
  const manager = Cypress.env('managerUsername');
  const managerPassword = Cypress.env('managerPassword');

  it('Успешный вход', () => {
    cy.visit('/auth/login');

    cy.get('#username').type(manager);
    cy.get('#password').type(managerPassword, { log: false });
    cy.get('input[type="submit"]').click();

    cy.url().should('include', '/api/ui/enterprises');
    cy.get('h1').should('contain', 'Список предприятий');
    cy.getCookie('JWT').should('exist');
  });

  it('Неверный пароль -> ошибка', () => {
    cy.visit('/auth/login');

    cy.get('#username').type(manager);
    cy.get('#password').type('заведомо-неверный-пароль');
    cy.get('input[type="submit"]').click();

    cy.url().should('include', '/auth/login?error');
    cy.get('.alert-danger').should('contain', 'Неверное имя пользователя или пароль');
    cy.getCookie('JWT').should('not.exist');
  });

  it('Несуществующий пользователь -> ошибка', () => {
    cy.visit('/auth/login');

    cy.get('#username').type('такого-пользователя-нет');
    cy.get('#password').type('любой-пароль');
    cy.get('input[type="submit"]').click();

    cy.url().should('include', '/auth/login?error');
    cy.get('.alert-danger').should('contain', 'Неверное имя пользователя или пароль');
    cy.getCookie('JWT').should('not.exist');
  });

  it('Отправка пустой формы -> повторный ввод', () => {
    cy.visit('/auth/login');

    cy.get('form').submit();

    cy.url().should('include', '/auth/login?error');
    cy.getCookie('JWT').should('not.exist');
  });

  it('Выход из системы -> удаление cookie и закрытие доступа', () => {
    cy.login();
    cy.getCookie('JWT').should('exist');

    cy.request({ method: 'POST', url: '/logout', followRedirect: false })
      .its('redirectedToUrl')
      .should('include', '/auth/login?logout');

    cy.getCookie('JWT').should('not.exist');
    cy.request({ url: '/api/ui/enterprises', failOnStatusCode: false })
      .its('status')
      .should('eq', 403);
  });

  it('Попытка открыть защищённую страницу без авторизации -> 403', () => {
    cy.request({ url: '/api/ui/enterprises', failOnStatusCode: false })
      .its('status')
      .should('eq', 403);
  });

  it('Попытка НЕменеджера получить доступ к списку предприятий', () => {
    cy.login(Cypress.env('userUsername'), Cypress.env('userPassword'));

    cy.visit('/api/ui/enterprises');

    cy.url().should('include', '/error/forbidden');
  });

  it('Страница входа для уже авторизованного пользователя', () => {
    cy.login();

    cy.visit('/auth/login');

    cy.get('#username').should('be.visible');
    cy.get('.alert-danger').should('not.exist');
  });
});
