import test from 'node:test';
import assert from 'node:assert/strict';
import axios from 'axios';
import { normalizeApiBaseUrl, normalizeRequestPath } from '../api.ts';

test('normalizeApiBaseUrl: correctly formats and normalizes production backend URLs', () => {
  const prodHost = 'https://ai-powered-campus-connect-backend.onrender.com';

  // Test standard production host URL
  assert.equal(
    normalizeApiBaseUrl(prodHost),
    'https://ai-powered-campus-connect-backend.onrender.com/api'
  );

  // Test host with trailing slash
  assert.equal(
    normalizeApiBaseUrl(`${prodHost}/`),
    'https://ai-powered-campus-connect-backend.onrender.com/api'
  );

  // Test host already ending with /api
  assert.equal(
    normalizeApiBaseUrl(`${prodHost}/api`),
    'https://ai-powered-campus-connect-backend.onrender.com/api'
  );

  // Test host ending with /api/
  assert.equal(
    normalizeApiBaseUrl(`${prodHost}/api/`),
    'https://ai-powered-campus-connect-backend.onrender.com/api'
  );

  // Test host with accidental duplicate /api/api
  assert.equal(
    normalizeApiBaseUrl(`${prodHost}/api/api`),
    'https://ai-powered-campus-connect-backend.onrender.com/api'
  );

  // Test localhost fallback
  assert.equal(
    normalizeApiBaseUrl('http://localhost:8080'),
    'http://localhost:8080/api'
  );

  assert.equal(
    normalizeApiBaseUrl('http://localhost:8080/api'),
    'http://localhost:8080/api'
  );
});

test('normalizeRequestPath: strips leading duplicate /api segments from request paths', () => {
  // Standard auth endpoints
  assert.equal(normalizeRequestPath('/auth/login'), '/auth/login');
  assert.equal(normalizeRequestPath('/auth/register'), '/auth/register');

  // Request paths with leading /api
  assert.equal(normalizeRequestPath('/api/auth/login'), '/auth/login');
  assert.equal(normalizeRequestPath('/api/auth/register'), '/auth/register');

  // Request paths without leading slash
  assert.equal(normalizeRequestPath('auth/login'), '/auth/login');
  assert.equal(normalizeRequestPath('api/auth/login'), '/auth/login');

  // Request paths with duplicate /api/api
  assert.equal(normalizeRequestPath('/api/api/auth/login'), '/auth/login');
  assert.equal(normalizeRequestPath('/api/api/auth/register'), '/auth/register');

  // Other endpoints
  assert.equal(normalizeRequestPath('/events'), '/events');
  assert.equal(normalizeRequestPath('/api/events'), '/events');
  assert.equal(normalizeRequestPath('/health'), '/health');
  assert.equal(normalizeRequestPath('/api/health'), '/health');
});

test('Axios URL resolution: guarantees login request URL uses /api/auth/login, not /api/api/auth/login', () => {
  const prodHost = 'https://ai-powered-campus-connect-backend.onrender.com';
  const baseURL = normalizeApiBaseUrl(prodHost);

  const client = axios.create({ baseURL });

  const applyInterceptor = (url: string) => {
    const config = { url, baseURL };
    config.url = normalizeRequestPath(config.url);
    return client.getUri(config);
  };

  // Verify normal auth/login call
  const resolvedUrl1 = applyInterceptor('/auth/login');
  assert.equal(resolvedUrl1, 'https://ai-powered-campus-connect-backend.onrender.com/api/auth/login');
  assert.notEqual(resolvedUrl1, 'https://ai-powered-campus-connect-backend.onrender.com/api/api/auth/login');

  // Verify caller passing /api/auth/login does NOT result in duplicate /api/api
  const resolvedUrl2 = applyInterceptor('/api/auth/login');
  assert.equal(resolvedUrl2, 'https://ai-powered-campus-connect-backend.onrender.com/api/auth/login');
  assert.notEqual(resolvedUrl2, 'https://ai-powered-campus-connect-backend.onrender.com/api/api/auth/login');

  // Verify caller passing api/auth/login without leading slash
  const resolvedUrl3 = applyInterceptor('api/auth/login');
  assert.equal(resolvedUrl3, 'https://ai-powered-campus-connect-backend.onrender.com/api/auth/login');

  // Verify caller passing duplicate /api/api/auth/login gets normalized cleanly
  const resolvedUrl4 = applyInterceptor('/api/api/auth/login');
  assert.equal(resolvedUrl4, 'https://ai-powered-campus-connect-backend.onrender.com/api/auth/login');

  // Verify registration URL
  const resolvedUrl5 = applyInterceptor('/auth/register');
  assert.equal(resolvedUrl5, 'https://ai-powered-campus-connect-backend.onrender.com/api/auth/register');
  assert.notEqual(resolvedUrl5, 'https://ai-powered-campus-connect-backend.onrender.com/api/api/auth/register');
});
