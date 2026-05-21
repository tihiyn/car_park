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
    const res = http.get('http://nginx/api/vehicles/25024', {
        headers: { Cookie: 'JWT=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLQkNC90LjRgdC40LzQvtCy0JLQoSIsImlhdCI6MTc3ODUwNzgyOCwiZXhwIjoxNzc4NTUxMDI4fQ.4gwUplsT2fa4D6soaitLvhYVQB9uuZHJRB_-sfyplRk' },
    });

    check(res, {
        'status 200': (r) => r.status === 200,
    });
}