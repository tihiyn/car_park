describe('Отчёты', () => {
  const vehicleWithTrips = { id: '14842', regNum: 'A083AE' };
  const enterpriseId = '1';

  const assertExportedFile = (fileName) => {
    cy.contains('a', 'Скачать отчёт').invoke('attr', 'href').then((href) => {
      cy.request(href).then((response) => {
        expect(response.status).to.eq(200);
        expect(response.headers['content-disposition']).to.contain(fileName);
        expect(response.headers['content-type']).to.contain('spreadsheetml');
        expect(response.body.length, 'файл не пустой').to.be.greaterThan(0);
      });
    });
  };

  beforeEach(() => {
    cy.login();
    cy.visit('/api/ui/reports/create');
  });

  it('Открытие формы для создания отчёта', () => {
    cy.get('h1').should('contain', 'Создание отчёта');
    cy.get('#reportForm').should('exist');
    cy.get('#reportType option').should('have.length', 4); // плейсхолдер + 3 типа
    cy.get('#vehicleId option').should('have.length.at.least', 2);
    cy.get('#enterpriseId option').should('have.length.at.least', 2);
  });

  it('Выбор типа отчёта', () => {
    cy.get('.report-fields').each(($group) => cy.wrap($group).should('not.be.visible'));

    cy.get('#reportType').select('mileage');
    cy.get('#vehicleId').should('be.visible');
    cy.get('#period').should('be.visible');
    cy.get('#enterpriseId').should('not.be.visible');

    cy.get('#reportType').select('enterpriseVehicles');
    cy.get('#enterpriseId').should('be.visible');
    cy.get('#beginYear').should('be.visible');
    cy.get('#vehicleId').should('not.be.visible');

    cy.get('#reportType').select('enterpriseDrivers');
    cy.get('#enterpriseId2').should('be.visible');
    cy.get('#beginYear').should('not.be.visible');
  });

  it('Построение и выгрузка отчёта о пробеге транспортного средства', () => {
    cy.get('#reportType').select('mileage');
    cy.get('#vehicleId').select(vehicleWithTrips.id);
    cy.get('#period').select('month');
    cy.get('#begin').type('2023-01-01T00:00');
    cy.get('#end').type('2026-01-01T00:00');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/api/ui/reports/vehicle/mileage');
    cy.get('h1').should('contain', 'Пробег автомобиля').and('contain', vehicleWithTrips.regNum);
    cy.get('tbody tr').should('have.length.at.least', 1);

    assertExportedFile('vehicle_mileage_report.xlsx');
  });

  it('Построение и выгрузка отчёта о кол-ве транспортных средств по годам', () => {
    cy.get('#reportType').select('enterpriseVehicles');
    cy.get('#enterpriseId').select(enterpriseId);
    cy.get('#beginYear').type('1990');
    cy.get('#endYear').type('2025');
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/api/ui/reports/enterprise/production');
    cy.get('h1').should('contain', 'ТрансАвто');
    cy.get('tbody tr').should('have.length.at.least', 1);

    assertExportedFile('enterprise_production_report.xlsx');
  });

  it('Построение и выгрузка отчёта о средней зарплате на предприятии', () => {
    cy.get('#reportType').select('enterpriseDrivers');
    cy.get('#enterpriseId2').select(enterpriseId);
    cy.get('button[type="submit"]').click();

    cy.url().should('include', '/api/ui/reports/enterprise/salary');
    cy.get('h1').should('contain', 'Средняя зарплата водителей');
    cy.get('tbody tr').should('have.length.at.least', 1);

    assertExportedFile('enterprise_salary_report.xlsx');
  });

  it('Отчёт по чужому предприятию не должен быть доступен', () => {
    cy.request({
      url: '/api/ui/reports/enterprise/salary?id=3',
      failOnStatusCode: false,
    }).its('status').should('eq', 403);
  });
});
