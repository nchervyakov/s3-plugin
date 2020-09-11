package io.jumpco.open.gradle.s3;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Transfer;
import org.gradle.api.logging.Logger;

import java.text.DecimalFormat;

public class S3Listener implements ProgressListener {

    private DecimalFormat df = new DecimalFormat("#0.0");
    private Transfer transfer;
    private Logger logger;

    public S3Listener(Transfer transfer, Logger logger) {
        this.transfer = transfer;
        this.logger = logger;
    }

    public void progressChanged(ProgressEvent e) {
        logger.info(String.format("%s%%", df.format(transfer.getProgress().getPercentTransferred())));
    }
}