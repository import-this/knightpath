package com.example.knightpath;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.gridlayout.widget.GridLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private enum State {
        WAITING,
        PLACED,
        DONE
    }
    private State state = State.WAITING;
    private int startI = -1, startJ = -1;
    private ImageView knightSquare;

    private GridLayout chessboard;
    private ImageView overlay;
    private int size;

    private void reset() {
        if (state == State.WAITING) return;
        state = State.WAITING;
        knightSquare.setImageDrawable(null);
        knightSquare = null;
        overlay.setImageBitmap(null);
    }

    private void setChessboard(GridLayout chessboard, int size) {
        // Layout parameters for all grid cells.
        final GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        // Column weight 1 for equal sized columns.
        final GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        chessboard.removeAllViews();    // Clear any previously created views.
        chessboard.setRowCount(size);
        chessboard.setColumnCount(size);

        Log.d(TAG,"Size: " + size);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                final SquareImageView square = new SquareImageView(this);
                // CAUTION: GridLayout.LayoutParams are mutable and must not be shared between views.
                final GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, colSpec);

                layoutParams.setGravity(Gravity.FILL);
                layoutParams.width = 0;     // 0 width to fill the column.

                square.setLayoutParams(layoutParams);
                square.setTag(new int[] {i, j});
                chessboard.addView(square);
                square.setOnClickListener(this);

                square.setBackgroundColor(ContextCompat.getColor(this,
                        (((i + j) % 2 == 0) ? R.color.squareWhite : R.color.squareBlack)));
            }
        }
        Log.d(TAG,"Created chessboard.");
    }

    public static class NoSolutionDialogFragment extends DialogFragment {
        private MainActivity mainActivity;

        public NoSolutionDialogFragment(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_no_solution_title)
                    .setMessage(R.string.dialog_no_solution)
                    .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mainActivity.reset();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chessboard = findViewById(R.id.chessboard);
        overlay = findViewById(R.id.overlay);

        final SeekBar boardSizeSelector = findViewById(R.id.boardsize);
        boardSizeSelector.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Change the chessboard size.
                size = getResources().getInteger(R.integer.board_size_min) + progress;

                reset();
                setChessboard(chessboard, size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Add the chessboard squares dynamically.
        size = getResources().getInteger(R.integer.board_size_min) + boardSizeSelector.getProgress();
        setChessboard(chessboard, size);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
                //Snackbar.make(view, "Game reset", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
    }

    private void drawLine(Canvas canvas, Paint paint, View sq1, View sq2) {
        canvas.drawLine(
                sq1.getLeft() + sq1.getWidth()/2,
                sq1.getTop() + sq1.getHeight()/2,
                sq2.getLeft() + sq2.getWidth()/2,
                sq2.getTop() + sq2.getHeight()/2,
                paint
        );
    }

    private void drawCircle(Canvas canvas, Paint paint, View sq) {
        canvas.drawCircle(
                sq.getLeft() + sq.getWidth()/2,
                sq.getTop() + sq.getHeight()/2,
                4 * getResources().getDisplayMetrics().density,
                paint
        );
    }

    private static final int[] COLORS = {
            android.R.color.darker_gray,
            android.R.color.holo_purple,
            android.R.color.holo_blue_dark,
            android.R.color.holo_blue_light,
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_orange_dark,
            android.R.color.holo_red_light,
            android.R.color.holo_red_dark
    };

    private static int colorIndex = 0;

    private void drawPaths(List<List<int[]>> solutions, int targetI, int targetJ) {
        final Bitmap bitmap = Bitmap.createBitmap(
                chessboard.getWidth(),
                chessboard.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        final Canvas canvas = new Canvas(bitmap);
        final Paint paint = new Paint();

        paint.setStyle(Paint.Style.FILL_AND_STROKE);    // For lines and circles.
        paint.setStrokeWidth(8);    // In pixels
        paint.setAntiAlias(true);

        if (BuildConfig.DEBUG && solutions.size() < 1) throw new AssertionError();
        for (final List<int[]> solution: solutions) {
            solution.add(new int[] {targetI, targetJ});
            if (BuildConfig.DEBUG && solution.size() < 2) throw new AssertionError();

            paint.setColor(ContextCompat.getColor(this, COLORS[colorIndex % COLORS.length]));
            colorIndex += 3;

            for (int k = 0; k < solution.size()-1; ++k) {
                final int[] pos1 = solution.get(k);
                final int[] pos2 = solution.get(k + 1);

                Log.d(TAG, ""+pos1[0] + " " + pos1[1] + " -> " + pos2[0] + " " + pos2[1]);
                final View sq1 = chessboard.getChildAt(pos1[0]*size + pos1[1]);
                final View sq3 = chessboard.getChildAt(pos2[0]*size + pos2[1]);
                View sq2;

                if (Math.abs(pos1[0] - pos2[0]) == 2) { // Long jump on rows.
                    sq2 = chessboard.getChildAt(pos2[0]*size + pos1[1]);
                } else {                                // Long jump on cols.
                    sq2 = chessboard.getChildAt(pos1[0]*size + pos2[1]);
                }
                // Draw long jump
                drawLine(canvas, paint, sq1, sq2);
                // Draw short jump
                drawLine(canvas, paint, sq2, sq3);
                // Mark end
                drawCircle(canvas, paint, sq3);
            }
        }
        overlay.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View v) {
        final ImageView square = (ImageView) v;
        final int[] tag = (int[]) v.getTag();
        int i = tag[0], j = tag[1];

        switch (state) {
        case PLACED:
            final List<List<int[]>> solutions = new KnightSolver(size).solve(startI, startJ, i, j);
            if (solutions.size() == 0) {    // No solution!
                DialogFragment newFragment = new NoSolutionDialogFragment(this);
                newFragment.show(getSupportFragmentManager(), "nosolution");
            } else {
                drawPaths(solutions, i, j);
                state = State.DONE;
            }
            break;
        case WAITING:
            state = State.PLACED;
            startI = i;
            startJ = j;
            knightSquare = square;
            // Show the knight's start position.
            square.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.knight));
            break;
        case DONE:
        default:
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
