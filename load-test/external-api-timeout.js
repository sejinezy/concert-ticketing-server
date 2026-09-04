import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    scenarios: {
        timeout_test: {
            executor: 'constant-vus',
            vus: 20,
            duration: '30s',
        },
    },
};

export default function () {
    http.post(
        'http://localhost:8080/test/payments/1',
        null,
        {
            timeout: '15s',
        }
    );

    sleep(1);
}