package com.android.commands.am;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.util.AndroidException;
import com.android.internal.os.BaseCommand;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class Am extends BaseCommand {
    private IActivityManager mAm;
    private IPackageManager mPm;

    static final class MyShellCallback extends ShellCallback {
        boolean mActive = true;

        MyShellCallback() {
        }

        public ParcelFileDescriptor onOpenFile(String path, String seLinuxContext, String mode) {
            if (this.mActive) {
                File file = new File(path);
                String tcon;
                try {
                    ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, 738197504);
                    if (seLinuxContext != null) {
                        tcon = SELinux.getFileContext(file.getAbsolutePath());
                        if (!SELinux.checkSELinuxAccess(seLinuxContext, tcon, "file", "write")) {
                            try {
                                fd.close();
                            } catch (IOException e) {
                            }
                            String msg = new StringBuilder();
                            msg.append("System server has no access to file context ");
                            msg.append(tcon);
                            msg = msg.toString();
                            PrintStream printStream = System.err;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(msg);
                            stringBuilder.append(" (from path ");
                            stringBuilder.append(file.getAbsolutePath());
                            stringBuilder.append(", context ");
                            stringBuilder.append(seLinuxContext);
                            stringBuilder.append(")");
                            printStream.println(stringBuilder.toString());
                            throw new IllegalArgumentException(msg);
                        }
                    }
                    return fd;
                } catch (FileNotFoundException e2) {
                    tcon = new StringBuilder();
                    tcon.append("Unable to open file ");
                    tcon.append(path);
                    tcon.append(": ");
                    tcon.append(e2);
                    tcon = tcon.toString();
                    System.err.println(tcon);
                    throw new IllegalArgumentException(tcon);
                }
            }
            PrintStream printStream2 = System.err;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Open attempt after active for: ");
            stringBuilder2.append(path);
            printStream2.println(stringBuilder2.toString());
            return null;
        }
    }

    public static void main(String[] args) {
        new Am().run(args);
    }

    public void onShowUsage(PrintStream out) {
        try {
            runAmCmd(new String[]{"help"});
        } catch (AndroidException e) {
            e.printStackTrace(System.err);
        }
    }

    public void onRun() throws Exception {
        this.mAm = ActivityManager.getService();
        if (this.mAm != null) {
            this.mPm = Stub.asInterface(ServiceManager.getService("package"));
            if (this.mPm == null) {
                System.err.println("Error type 2");
                throw new AndroidException("Can't connect to package manager; is the system running?");
            } else if (nextArgRequired().equals("instrument")) {
                runInstrument();
                return;
            } else {
                runAmCmd(getRawArgs());
                return;
            }
        }
        System.err.println("Error type 2");
        throw new AndroidException("Can't connect to activity manager; is the system running?");
    }

    int parseUserArg(String arg) {
        if ("all".equals(arg)) {
            return -1;
        }
        if ("current".equals(arg) || "cur".equals(arg)) {
            return -2;
        }
        return Integer.parseInt(arg);
    }

    void runAmCmd(String[] args) throws AndroidException {
        MyShellCallback cb = new MyShellCallback();
        try {
            this.mAm.asBinder().shellCommand(FileDescriptor.in, FileDescriptor.out, FileDescriptor.err, args, cb, new ResultReceiver(null) {
            });
            cb.mActive = false;
        } catch (RemoteException e) {
            System.err.println("Error type 2");
            throw new AndroidException("Can't call activity manager; is the system running?");
        } catch (Throwable th) {
            cb.mActive = false;
        }
    }

    public void runInstrument() throws Exception {
        Instrument instrument = new Instrument(this.mAm, this.mPm);
        while (true) {
            String nextOption = nextOption();
            String opt = nextOption;
            if (nextOption != null) {
                if (opt.equals("-p")) {
                    instrument.profileFile = nextArgRequired();
                } else if (opt.equals("-w")) {
                    instrument.wait = true;
                } else if (opt.equals("-r")) {
                    instrument.rawMode = true;
                } else if (opt.equals("-m")) {
                    instrument.protoStd = true;
                } else if (opt.equals("-f")) {
                    instrument.protoFile = true;
                    if (!(peekNextArg() == null || peekNextArg().startsWith("-"))) {
                        instrument.logPath = nextArg();
                    }
                } else if (opt.equals("-e")) {
                    instrument.args.putString(nextArgRequired(), nextArgRequired());
                } else if (opt.equals("--no_window_animation") || opt.equals("--no-window-animation")) {
                    instrument.noWindowAnimation = true;
                } else if (opt.equals("--no-hidden-api-checks")) {
                    instrument.disableHiddenApiChecks = true;
                } else if (opt.equals("--user")) {
                    instrument.userId = parseUserArg(nextArgRequired());
                } else if (opt.equals("--abi")) {
                    instrument.abi = nextArgRequired();
                } else {
                    PrintStream printStream = System.err;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error: Unknown option: ");
                    stringBuilder.append(opt);
                    printStream.println(stringBuilder.toString());
                    return;
                }
            } else if (instrument.userId == -1) {
                System.err.println("Error: Can't start instrumentation with user 'all'");
                return;
            } else {
                instrument.componentNameArg = nextArgRequired();
                instrument.run();
                return;
            }
        }
    }
}
