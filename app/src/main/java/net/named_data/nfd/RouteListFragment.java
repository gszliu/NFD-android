/* -*- Mode:jde; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2015 Regents of the University of California
 *
 * This file is part of NFD (Named Data Networking Forwarding Daemon) Android.
 * See AUTHORS.md for complete list of NFD Android authors and contributors.
 *
 * NFD Android is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NFD Android is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NFD Android, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.named_data.nfd;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.intel.jndn.management.types.RibEntry;
import com.intel.jndn.management.types.Route;

import net.named_data.jndn.Name;
import net.named_data.jndn_xx.util.FaceUri;
import net.named_data.nfd.utils.Nfdc;

import java.util.ArrayList;
import java.util.List;

public class RouteListFragment extends ListFragment implements RouteCreateDialogFragment.OnRouteCreateRequested {

  public static RouteListFragment
  newInstance() {
    return new RouteListFragment();
  }

  @Override
  public void
  onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setListAdapter(new RouteListAdapter(getActivity()));
  }

  @Override
  public View
  onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState)
  {
    View v = inflater.inflate(R.layout.fragment_route_list, null);
    m_routeListInfoUnavailableView = v.findViewById(R.id.route_list_info_unavailable);

    // Get progress bar spinner view
    m_reloadingListProgressBar
      = (ProgressBar)v.findViewById(R.id.route_list_reloading_list_progress_bar);

    Button refreshRouteListButton = (Button) v.findViewById(R.id.route_list_refresh_button);
    refreshRouteListButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        retrieveRouteList();
      }
    });

    Button addRouteButton = (Button)v.findViewById(R.id.route_list_add_button);
    addRouteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view)
      {
        RouteCreateDialogFragment dialog = RouteCreateDialogFragment.newInstance();
        dialog.setTargetFragment(RouteListFragment.this, 0);
        dialog.show(getFragmentManager(), "RouteCreateFragment");
      }
    });

    return v;
  }

  @Override
  public void
  onResume() {
    super.onResume();
    startRouteListInfoRetrievalTask();
  }

  @Override
  public void
  onPause() {
    super.onPause();
    stopRouteListInfoRetrievalTask();

    if (m_routeCreateAsyncTask != null) {
      m_routeCreateAsyncTask.cancel(false);
      m_routeCreateAsyncTask = null;
    }
  }

  @Override
  public void
  createRoute(Name prefix, String faceUri)
  {
    m_routeCreateAsyncTask = new RouteCreateAsyncTask(prefix, faceUri);
    m_routeCreateAsyncTask.execute();
  }


  /////////////////////////////////////////////////////////////////////////

  /**
   * Updates the underlying adapter with the given list of RibEntry.
   *
   * Note: This method should only be called from the UI thread.
   *
   * @param list Update ListView with the given List&lt;RibEntry&gt;
   */
  private void updateRouteList(List<RibEntry> list) {
    if (list == null) {
      m_routeListInfoUnavailableView.setVisibility(View.VISIBLE);
      return;
    }

    ((RouteListAdapter)getListAdapter()).updateList(list);
  }

  /**
   * Convenience method that starts the AsyncTask that retrieves the
   * list of available routes.
   */
  private void retrieveRouteList() {
    // Update UI
    m_routeListInfoUnavailableView.setVisibility(View.GONE);

    // Stop if running; before starting the new Task
    stopRouteListInfoRetrievalTask();
    startRouteListInfoRetrievalTask();
  }

  /**
   * Create a new AsynTask for route list information retrieval.
   */
  private void startRouteListInfoRetrievalTask() {
    m_routeListAsyncTask = new RouteListAsyncTask();
    m_routeListAsyncTask.execute();
  }

  /**
   * Stops a previously started AsyncTask.
   */
  private void stopRouteListInfoRetrievalTask() {
    if (m_routeListAsyncTask != null) {
      m_routeListAsyncTask.cancel(false);
      m_routeListAsyncTask = null;
    }
  }

  /////////////////////////////////////////////////////////////////////////

  private static class RouteListAdapter extends BaseAdapter {

    public RouteListAdapter(Context context) {
      m_layoutInflater = LayoutInflater.from(context);
    }

    public void
    updateList(List<RibEntry> ribEntries) {
      m_ribEntries = ribEntries;
      notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
      return (m_ribEntries == null) ? 0 : m_ribEntries.size();
    }

    @Override
    public RibEntry
    getItem(int i)
    {
      assert m_ribEntries != null;
      return m_ribEntries.get(i);
    }

    @Override
    public long
    getItemId(int i)
    {
      return i;
    }

    @Override
    public View
    getView(int position, View convertView, ViewGroup parent) {
      RouteItemHolder holder;

      if (convertView == null) {
        holder = new RouteItemHolder();

        convertView = m_layoutInflater.inflate(R.layout.list_item_route_item, null);
        convertView.setTag(holder);

        holder.m_uri = (TextView) convertView.findViewById(R.id.list_item_route_uri);
        holder.m_faceList = (TextView) convertView.findViewById(R.id.list_item_face_list);
      } else {
        holder = (RouteItemHolder) convertView.getTag();
      }

      RibEntry entry = getItem(position);

      // Prefix
      holder.m_uri.setText(entry.getName().toUri());

      // List of faces
      List<String> faceList = new ArrayList<>();
      for (Route r : entry.getRoutes()) {
        faceList.add(String.valueOf(r.getFaceId()));
      }
      holder.m_faceList.setText(TextUtils.join(", ", faceList));

      return convertView;
    }

    private static class RouteItemHolder {
      private TextView m_uri;
      private TextView m_faceList;
    }

    private final LayoutInflater m_layoutInflater;
    private List<RibEntry> m_ribEntries;
  }

  private class RouteListAsyncTask extends AsyncTask<Void, Void, Pair<List<RibEntry>, Exception>> {
    @Override
    protected void
    onPreExecute() {
      // Display progress bar
      m_reloadingListProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Pair<List<RibEntry>, Exception>
    doInBackground(Void... params) {
      Nfdc nfdc = new Nfdc();
      Exception returnException = null;
      List<RibEntry> routes = null;
      try {
        routes = nfdc.ribList();
      }
      catch (Exception e) {
        returnException = e;
      }
      nfdc.shutdown();
      return new Pair<>(routes, returnException);
    }

    @Override
    protected void onCancelled() {
      // Remove progress bar
      m_reloadingListProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onPostExecute(Pair<List<RibEntry>, Exception> result) {
      // Remove progress bar
      m_reloadingListProgressBar.setVisibility(View.GONE);

      if (result.second != null) {
        Toast.makeText(getActivity(),
                       "Error communicating with NFD (" + result.second.getMessage() + ")",
                       Toast.LENGTH_LONG).show();
      }

      updateRouteList(result.first);
    }
  }


  private class RouteCreateAsyncTask extends AsyncTask<Void, Void, String> {
    public
    RouteCreateAsyncTask(Name prefix, String faceUri)
    {
      m_prefix = prefix;
      m_faceUri = faceUri;
    }

    @Override
    protected String
    doInBackground(Void... params)
    {
      try {
        Nfdc nfdc = new Nfdc();
        int faceId = nfdc.faceCreate(m_faceUri);
        boolean ok = nfdc.ribRegisterPrefix(new Name(m_prefix), faceId, 10, true, false);
        nfdc.shutdown();
        if (ok) {
          return "OK";
        }
        else {
          return "Failed register prefix";
        }
      } catch (FaceUri.CanonizeError e) {
        return "Error creating face (" + e.getMessage() + ")";
      } catch (FaceUri.Error e) {
        return "Error creating face (" + e.getMessage() + ")";
      }
      catch (Exception e) {
        return "Error communicating with NFD (" + e.getMessage() + ")";
      }
    }

    @Override
    protected void
    onPreExecute()
    {
      // Display progress bar
      m_reloadingListProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void
    onPostExecute(String status)
    {
      // Display progress bar
      m_reloadingListProgressBar.setVisibility(View.VISIBLE);
      Toast.makeText(getActivity(), status, Toast.LENGTH_LONG).show();

      retrieveRouteList();
    }

    @Override
    protected void
    onCancelled()
    {
      // Remove progress bar
      m_reloadingListProgressBar.setVisibility(View.GONE);
    }

    ///////////////////////////////////////////////////////////////////////////

    private Name m_prefix;
    private String m_faceUri;
  }

  /////////////////////////////////////////////////////////////////////////////

  /** Reference to the most recent AsyncTask that was created for listing routes */
  private RouteListAsyncTask m_routeListAsyncTask;

  /** Reference to the view to be displayed when no information is available */
  private View m_routeListInfoUnavailableView;

  /** Progress bar spinner to display to user when destroying faces */
  private ProgressBar m_reloadingListProgressBar;

  /** Reference to the most recent AsyncTask that was created for creating a route */
  private RouteCreateAsyncTask m_routeCreateAsyncTask;
}