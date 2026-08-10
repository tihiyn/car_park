/**
 * Быстрый логин в обход UI: POST /auth/login form-urlencoded.
 * CSRF в приложении отключён, сервер в ответ ставит HttpOnly-cookie JWT,
 * которую Cypress дальше подставляет во все запросы браузера.
 */
Cypress.Commands.add('login', (
  username = Cypress.env('managerUsername'),
  password = Cypress.env('managerPassword'),
) => {
  cy.request({
    method: 'POST',
    url: '/auth/login',
    form: true,
    body: { username, password },
  });
});

/**
 * Создаёт одноразовое ТС через REST и отдаёт его id.
 * Нужно там, где тест не может позволить себе трогать боевые данные:
 * тесты идут против рабочей БД, поэтому меняем только то, что создали сами.
 */
Cypress.Commands.add('createVehicle', (overrides = {}) => {
  const body = {
    regNum: 'E2E000',
    price: 1000000,
    mileage: 1000,
    productionYear: 2020,
    color: 'Синий',
    isAvailable: true,
    brandId: 1,
    enterpriseId: 1,
    driverIds: [],
    ...overrides,
  };
  return cy.request('POST', '/api/vehicles/new', body)
    .then(() => cy.request('/api/vehicles?size=1&sort=id,desc').its('body.0.id'));
});

/** Удаляет ТС, если оно ещё существует. Безопасно вызывать в afterEach. */
Cypress.Commands.add('deleteVehicleIfExists', (id) => {
  if (!id) {
    return;
  }
  cy.request({ method: 'DELETE', url: `/api/vehicles/${id}`, failOnStatusCode: false });
});
