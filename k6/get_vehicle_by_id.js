import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m',  target: 10 },
        { duration: '15s', target: 0 },
    ],
};

export default function () {
    const token = __ENV.JWT;
    const res = http.get('http://nginx/api/vehicles/25024', {
        headers: { Cookie: token },
    });

    check(res, {
        'status 200': (r) => r.status === 200,
    });
}