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

Cypress.Commands.add('deleteVehicleIfExists', (id) => {
  if (!id) {
    return;
  }
  cy.request({ method: 'DELETE', url: `/api/vehicles/${id}`, failOnStatusCode: false });
});
