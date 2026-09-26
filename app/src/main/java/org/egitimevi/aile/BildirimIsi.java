package org.egitimevi.aile;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Android'in zamanladığı bildirim yoklaması (bkz. Bildirimler). */
public class BildirimIsi extends JobService {
    @Override
    public boolean onStartJob(JobParameters p) {
        new Thread(() -> {
            try {
                Bildirimler.yokla(getApplicationContext());
            } finally {
                jobFinished(p, false);
                /* Servis saatindeysek bir dakika sonrasına yeni tek seferlik iş kurulur. */
                Bildirimler.zamanla(getApplicationContext(), true);
            }
        }, "bildirim-yoklama").start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters p) {
        return true;
    }
}
