import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '10s', target: 50 },
        { duration: '20s', target: 100 },
        { duration: '20s', target: 200 },
        { duration: '20s', target: 300 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<200'],
    },
};

export default function () {
    const res = http.get('http://nginx/test/cpu');

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.001);
}