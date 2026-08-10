describe('Поездки', () => {
  const vehicleWithTrips = '14842';

  beforeEach(() => {
    cy.login();
  });

  describe('Получение списка поездок за период', () => {
    beforeEach(() => {
      cy.visit(`/vehicles/${vehicleWithTrips}`);
    });

    it('Получение поездок в выбранном диапазоне', () => {
      cy.intercept('GET', '/trips?*').as('loadTrips');

      cy.get('#fromDate').type('2023-01-01T00:00');
      cy.get('#toDate').type('2026-01-01T00:00');
      cy.get('#loadTrips').click();
      cy.wait('@loadTrips', { timeout: 120000 });

      cy.get('#trips .list-group-item').should('have.length.at.least', 1);
      cy.get('#trips').should('contain', 'Начало:').and('contain', 'Конец:');
      cy.get('#showOnMap').should('be.visible');
    });

    it('Нет доступных поездок за указанный период', () => {
      cy.get('#fromDate').type('1990-01-01T00:00');
      cy.get('#toDate').type('1990-12-31T00:00');
      cy.get('#loadTrips').click();

      cy.get('#trips .alert-warning').should('contain', 'Нет поездок в выбранном диапазоне');
      cy.get('#trips .list-group-item').should('not.exist');
    });
  });

  describe('Загрузка поездки из файла', () => {
    let createdVehicleId;

    beforeEach(() => {
      cy.createVehicle({ regNum: 'E2E010' }).then((id) => {
        createdVehicleId = id;
        cy.visit(`/vehicles/${id}`);
      });
    });

    afterEach(() => {
      cy.login();
      cy.deleteVehicleIfExists(createdVehicleId);
      createdVehicleId = null;
    });

    it('Загрузка GPX-файла и появление поездки в списке', () => {
      cy.get('#tripFile').selectFile('cypress/fixtures/trip.gpx');
      cy.get('#uploadTripForm button[type="submit"]').click();

      cy.get('#uploadStatus', { timeout: 90000 })
        .should('contain', 'Файл успешно загружен')
        .and('have.class', 'text-success');

      cy.intercept('GET', '/trips?*').as('loadTrips');
      cy.get('#fromDate').type('2024-01-01T00:00');
      cy.get('#toDate').type('2024-12-31T00:00');
      cy.get('#loadTrips').click();
      cy.wait('@loadTrips', { timeout: 120000 });

      cy.get('#trips .list-group-item').should('have.length', 1);
    });

    it('Попытка загрузки файла в неверном формате', () => {
      cy.get('#tripFile').selectFile('cypress/fixtures/not-a-trip.txt', { force: true });
      cy.get('#uploadTripForm button[type="submit"]').click();

      cy.get('#uploadStatus')
        .should('contain', 'Ошибка загрузки')
        .and('have.class', 'text-danger');

      cy.request(`/api/trips/${createdVehicleId}/trips?begin=2024-01-01T00:00:00Z&end=2025-01-01T00:00:00Z`)
        .its('body')
        .should('have.length', 0);
    });
  });

  it('Отслеживание движения транспортного средства на карте онлайн ', () => {
    cy.visit(`/vehicles/${vehicleWithTrips}/online-map`, {
      onBeforeLoad(win) {
        cy.stub(win, 'EventSource').as('eventSource').returns({ close() {} });
      },
    });

    cy.get('h2').should('contain', 'Отслеживание автомобиля');
    cy.get('#map').should('be.visible');
    cy.get('@eventSource').should('have.been.calledWith', `/api/trips/${vehicleWithTrips}/online`);
  });
});
