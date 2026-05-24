import http from 'k6/http';
import { check } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 200 },
        { duration: '1m',  target: 200 },
        { duration: '15s', target: 0 },
    ],
};

export default function () {
    const token = __ENV.JWT;
    const res = http.get('http://nginx/api/vehicles/25024', {
        headers: { Cookie: token },
    });

    if (res.status !== 200) {
        console.log(`Error: status=${res.status} body=${res.body}`);
    }

    check(res, {
        'status 200': (r) => r.status === 200,
    });
}