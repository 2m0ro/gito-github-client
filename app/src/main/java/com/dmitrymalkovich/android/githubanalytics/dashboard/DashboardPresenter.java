package com.dmitrymalkovich.android.githubanalytics.dashboard;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.dmitrymalkovich.android.githubanalytics.data.source.GithubDataSource;
import com.dmitrymalkovich.android.githubanalytics.data.source.GithubRepository;
import com.dmitrymalkovich.android.githubanalytics.data.source.LoaderProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class DashboardPresenter implements DashboardContract.Presenter,
        LoaderManager.LoaderCallbacks<Cursor>, GithubRepository.LoadDataCallback {

    @SuppressWarnings("unused")
    private static String LOG_TAG = DashboardPresenter.class.getSimpleName();
    private static final int REPOSITORIES_LOADER = 1;

    @SuppressWarnings("unused")
    @NonNull
    private final LoaderProvider mLoaderProvider;

    @NonNull
    private final LoaderManager mLoaderManager;

    @NonNull
    private GithubRepository mGithubRepository;

    @NonNull
    private DashboardContract.View mDashboardView;

    public DashboardPresenter(@NonNull GithubRepository githubRepository,
                              @NonNull DashboardContract.View view,
                              @NonNull LoaderProvider loaderProvider,
                              @NonNull LoaderManager loaderManager) {
        mGithubRepository = checkNotNull(githubRepository);
        mDashboardView = checkNotNull(view);
        mLoaderProvider = checkNotNull(loaderProvider);
        mLoaderManager = checkNotNull(loaderManager, "loaderManager cannot be null!");
        mDashboardView.setPresenter(this);
    }

    @Override
    public void start() {
        mDashboardView.setLoadingIndicator(true);
        showRepositories();
    }

    @Override
    public void onRefresh() {
        mGithubRepository.getRepositories(new GithubDataSource.GetRepositoriesCallback() {
            @Override
            public void onRepositoriesLoaded() {
                mDashboardView.setLoadingIndicator(false);
            }

            @Override
            public void onDataNotAvailable() {
                mDashboardView.setLoadingIndicator(false);
            }
        });
    }

    @Override
    public void onDataLoaded(Cursor data) {
        mDashboardView.setLoadingIndicator(false);
        mDashboardView.showRepositories(data);
    }

    @Override
    public void onDataEmpty() {
    }

    @Override
    public void onDataNotAvailable() {

    }

    @Override
    public void onDataReset() {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.createPopularRepositoryLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            if (data.moveToLast()) {
                onDataLoaded(data);
            } else {
                onDataEmpty();
            }
        } else {
            onDataNotAvailable();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onDataReset();
    }

    private void showRepositories() {
        mLoaderManager.initLoader(REPOSITORIES_LOADER,
                null,
                DashboardPresenter.this);
    }
}