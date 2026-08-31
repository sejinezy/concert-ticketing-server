import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    scenarios: {
        retry_storm: {
            executor: 'constant-vus',
            vus: 100,
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