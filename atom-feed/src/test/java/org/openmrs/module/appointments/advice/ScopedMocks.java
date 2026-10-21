package org.openmrs.module.appointments.advice;

import org.mockito.ScopedMock;

final class ScopedMocks {

    private ScopedMocks() {
    }

    /**
     * Closes every non-null mock even if an earlier close fails, so that a failing
     * close cannot leave later registrations open and break the next test.
     */
    static void closeAll(ScopedMock... mocks) {
        RuntimeException failure = null;
        for (ScopedMock mock : mocks) {
            if (mock == null) {
                continue;
            }
            try {
                mock.close();
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
