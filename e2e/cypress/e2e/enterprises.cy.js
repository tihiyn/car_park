describe('Предприятия', () => {
  beforeEach(() => {
    cy.login();
  });

  it('Получение списка предприятий', () => {
    cy.visit('/api/ui/enterprises');

    cy.get('h1').should('contain', 'Список предприятий');
    cy.get('thead th').should('have.length', 7);
    cy.get('thead th').first().should('contain', 'ID');
    cy.get('thead th').last().should('contain', 'Транспорт');

    cy.get('tbody tr').should('have.length.at.least', 1);
    cy.get('tbody tr').first().within(() => {
      cy.get('.timezone-select').should('exist');
      cy.get('.badge.bg-primary').should('have.length.at.least', 1); // водители
      cy.get('.vehicle-badge').should('have.length.at.least', 1); // ТС
    });
  });

  it('Менеджеру видны только принадлежащие ему предприятия', () => {
    cy.visit('/api/ui/enterprises');

    cy.get('tbody').should('contain', 'ТрансАвто');
    cy.get('tbody').should('contain', 'Северный путь');
    cy.get('tbody').should('not.contain', 'Магистраль');
  });

  it('Проверка пагинации списка предприятий', () => {
    cy.visit('/api/ui/enterprises?size=1&page=0');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr').first().should('contain', 'ТрансАвто');

    cy.visit('/api/ui/enterprises?size=1&page=1');
    cy.get('tbody tr').should('have.length', 1);
    cy.get('tbody tr').first().should('contain', 'Северный путь');
  });

  it('Клик по номеру транспортного средства', () => {
    cy.visit('/api/ui/enterprises?size=1&page=0');

    cy.get('.vehicle-badge a').first().click();

    cy.url().should('match', /\/vehicles\/\d+$/);
    cy.get('h2').should('contain', 'Информация об автомобиле');
  });

  it('Нажатие на «+» для добавления нового транспортного средства в предприятие', () => {
    cy.visit('/api/ui/enterprises?size=1&page=0');

    cy.get('tbody tr').first().find('a.bg-secondary').click();

    cy.url().should('include', '/vehicles/new?eId=1');
    cy.get('h1').should('contain', 'Добавить транспортное средство');
  });

  describe('Смена часового пояса предприятия', () => {
    const enterpriseId = 1;
    const originalTimeZone = 'America/Chicago';

    afterEach(() => {
      cy.login();
      cy.request({
        method: 'POST',
        url: '/api/ui/enterprises/update-timezone',
        form: true,
        body: { eId: enterpriseId, timeZone: originalTimeZone },
      });
    });

    it('Сохранение выбранного часового пояса предприятия', () => {
      cy.visit('/api/ui/enterprises?size=1&page=0');
      cy.get('.timezone-select').should('have.value', originalTimeZone);

      cy.get('.timezone-select').select('Europe/Moscow');

      cy.url().should('include', '/api/ui/enterprises');
      cy.get('tbody tr').first().find('.timezone-select').should('have.value', 'Europe/Moscow');
    });
  });
});
