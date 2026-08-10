describe('Транспортные средства', () => {
  const foreignVehicleId = 20020;

  beforeEach(() => {
    cy.login();

    cy.request('/api/vehicles?size=1').its('body.0.id').as('vehicleId');
  });

  it('Открытие страницы с информацией об транспортном средстве', () => {
    cy.get('@vehicleId').then((id) => cy.visit(`/vehicles/${id}`));

    cy.get('h2').first().should('contain', 'Информация об автомобиле');
    cy.get('#regNum').should('have.attr', 'readonly').and('not.be.empty');
    cy.get('#price').should('have.attr', 'readonly');
    cy.get('#mileage').should('have.attr', 'readonly');
    cy.get('#productionYear').should('have.attr', 'readonly');
    cy.get('#color').should('have.attr', 'readonly');
    cy.contains('a', 'Редактировать').should('be.visible');
    cy.get('#watchBtn').should('be.visible');
  });

  it('Нажатие на кнопку «Наблюдать» для перехода на онлайн-карту', () => {
    cy.get('@vehicleId').then((id) => {
      cy.visit(`/vehicles/${id}`);
      cy.get('#watchBtn').click();
      cy.url().should('include', `/vehicles/${id}/online-map`);
    });
  });

  it('Нажатие на кнопку «Редактировать»', () => {
    cy.get('@vehicleId').then((id) => {
      cy.visit(`/vehicles/${id}`);
      cy.contains('a', 'Редактировать').click();
      cy.url().should('include', `/vehicles/edit/${id}`);
    });

    cy.get('#regNum').should('not.have.value', '');
    cy.get('#price').should('not.have.value', '');
    cy.get('#brand').find('option:selected').should('exist');
    cy.get('input[name="id"]').should('exist');
  });

  it('Нажатие на кнопку «К списку предприятий»', () => {
    cy.get('@vehicleId').then((id) => cy.visit(`/vehicles/${id}`));

    cy.contains('a', 'К списку предприятий').click();

    cy.url().should('include', '/api/ui/enterprises');
    cy.get('h1').should('contain', 'Список предприятий');
  });

  it('Попытка получить информацию о транспортном средстве, не относящемуся к предприятиям менеджера', () => {
    cy.request({ url: `/vehicles/${foreignVehicleId}`, failOnStatusCode: false }).then((response) => {
      expect(response.status).to.eq(403);
      expect(response.body.message).to.contain('не относится к Вашим предприятиям');
    });
  });

  describe('Страница для создания нового транспортного средства', () => {
    beforeEach(() => {
      cy.visit('/vehicles/new?eId=1');
    });

    it('Открытие страницы для создания', () => {
      cy.get('h1').should('contain', 'Добавить транспортное средство');
      cy.get('#regNum').should('be.empty');
      cy.get('#brand option').should('have.length.at.least', 2); // плейсхолдер + бренды
      cy.get('#drivers option').should('have.length.at.least', 1);
      cy.get('#activeDriver').should('be.disabled');
    });

    it('Попытка отправить с пустым регистрационным номером', () => {
      cy.get('#price').type('1000000');
      cy.get('button[type="submit"]').click();

      cy.get('#regNum').then(($input) => {
        expect($input[0].checkValidity(), 'поле regNum невалидно').to.be.false;
      });
      cy.url().should('include', '/vehicles/new');
    });

    it('Попытка отправить с ценой ниже минимальной', () => {
      cy.get('#regNum').type('E2E001');
      cy.get('#price').type('1000'); // min=100000
      cy.get('button[type="submit"]').click();

      cy.get('#price').then(($input) => {
        expect($input[0].checkValidity(), 'поле price невалидно').to.be.false;
      });
      cy.url().should('include', '/vehicles/new');
    });
  });


  describe('Создание/изменение/удаление транспортного средства', () => {
    let createdVehicleId;

    afterEach(() => {
      cy.login();
      cy.deleteVehicleIfExists(createdVehicleId);
      createdVehicleId = null;
    });

    it('Заполнение формы для создания и отправка', () => {
      cy.visit('/vehicles/new?eId=1');

      cy.get('#regNum').type('E2E001');
      cy.get('#price').type('1000000');
      cy.get('#mileage').type('1000');
      cy.get('#productionYear').type('2020');
      cy.get('#color').type('Изумрудный');
      cy.get('#brand').select(1);

      cy.get('#activeDriver option').eq(1).invoke('val').then((driverId) => {
        cy.get('#drivers').select(driverId, { force: true });
        cy.window().then((win) => win.$('#drivers').trigger('changed.bs.select'));
        cy.get('#activeDriver').select(driverId);
      });

      cy.get('button[type="submit"]').click();

      cy.url().should('include', '/api/ui/enterprises');
      cy.request('/api/vehicles?size=1&sort=id,desc').its('body.0').then((created) => {
        createdVehicleId = created.id;
        expect(created.regNum).to.eq('E2E001');
        expect(created.color).to.eq('Изумрудный');
        expect(created.enterpriseId).to.eq(1);
        expect(created.activeDriverId).to.not.be.null;
      });
    });

    it('Редактирование цвета и пробега транспортного средства', () => {
      cy.createVehicle({ regNum: 'E2E002', color: 'Синий' }).then((id) => {
        createdVehicleId = id;

        cy.visit(`/vehicles/edit/${id}`);
        cy.get('#color').clear().type('Изумрудный');
        cy.get('#mileage').clear().type('7000');
        cy.get('#datetimeInput').type('2020-01-01T10:00');
        cy.get('button[type="submit"]').click();

        cy.url().should('include', '/api/ui/enterprises');
        cy.request(`/api/vehicles/${id}`).its('body').should((v) => {
          expect(v.color).to.eq('Изумрудный');
          expect(v.mileage).to.eq(7000);
        });
      });
    });

    it('Нажатие на "x" и удаление транспортного средства', () => {
      cy.createVehicle({ regNum: 'E2E003' }).then((id) => {
        createdVehicleId = id;

        cy.visit('/api/ui/enterprises?size=1&page=0');
        cy.contains('.vehicle-badge', 'E2E003').find('.btn-close').click();

        cy.contains('.vehicle-badge', 'E2E003').should('not.exist');
        cy.request({ url: `/api/vehicles/${id}`, failOnStatusCode: false })
          .its('status')
          .should('eq', 404);
      });
    });
  });
});
