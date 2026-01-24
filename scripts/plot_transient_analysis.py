import pandas as pd
import matplotlib.pyplot as plt
import sys
import os


# Lancia: py scripts/plot_transient_analysis.py output/transient-analysis-base-doubleMedMeanTime ReducedLambda
# Lancia: py scripts/plot_transient_analysis.py output/transient-analysis-base-medMeanTime NormalLambda


# CONFIGURAZIONE
NUM_REPLICATIONS_TO_PLOT = 7
RUN_FOR_CENTER_COMPARISON = 0


def generate_transient_plots(experiment_path, filename_suffix=""):
   runs_dir = os.path.join(experiment_path, "runs-samples")


   if not os.path.exists(runs_dir):
       print(f"ERRORE: Cartella runs-samples non trovata in: {experiment_path}")
       return


   print(f"--- Generazione Plot per: {experiment_path} (Suffisso: '{filename_suffix}') ---")


   output_dir = os.path.join(experiment_path, "plot")
   os.makedirs(output_dir, exist_ok=True)


   # =================================================================================
   # PARTE 1: SPAGHETTI PLOT (Confronto tra SEED diversi)
   # =================================================================================
   collected_data = {
       'System_Ts': [],
       'XRay_Ts': [],
       'XRay_Nq': []
   }


   for i in range(NUM_REPLICATIONS_TO_PLOT):
       csv_path = os.path.join(runs_dir, f"run-{i}", "sample.csv")


       if not os.path.exists(csv_path):
           continue


       try:
           df = pd.read_csv(csv_path, sep=',')
           df.columns = df.columns.str.strip()
           if 'Metric' in df.columns: df['Metric'] = df['Metric'].str.strip()
           if 'Center' in df.columns: df['Center'] = df['Center'].str.strip()


           # A) E[Ts] System
           sys_ts = df[df['Metric'] == 'SystemResponseTime_Success'].copy()
           sys_ts['RunID'] = i
           collected_data['System_Ts'].append(sys_ts)


           # B) E[Ts] XRay (TimeTotal)
           xray_ts = df[(df['Metric'] == 'TimeTotal') & (df['Center'] == 'XRay')].copy()
           xray_ts['RunID'] = i
           collected_data['XRay_Ts'].append(xray_ts)


           # C) E[Nq] XRay (NumQueue)
           xray_nq = df[(df['Metric'] == 'NumQueue') & (df['Center'] == 'XRay')].copy()
           xray_nq['RunID'] = i
           collected_data['XRay_Nq'].append(xray_nq)


       except Exception as e:
           print(f"ERRORE leggendo Run {i}: {e}")


   def plot_seeds(data_list, title, filename, y_label):
       if not data_list: return
       plt.figure(figsize=(12, 7))
       colors = plt.cm.tab10(range(NUM_REPLICATIONS_TO_PLOT))


       for idx, df_run in enumerate(data_list):
           run_id = df_run['RunID'].iloc[0]
           plt.plot(df_run['Time'], df_run['Value'], label=f"Seed #{run_id}",
                    color=colors[idx], linewidth=1.5, alpha=0.75)


       plt.title(f"Transient Analysis (Seeds): {title}", fontsize=14, fontweight='bold')
       plt.xlabel("Tempo Simulazione (s)")
       plt.ylabel(y_label)
       plt.legend(loc='upper right', title="Replicazioni")
       plt.grid(True, linestyle='--', alpha=0.4)
       plt.savefig(os.path.join(output_dir, filename), dpi=200, bbox_inches='tight')
       plt.close()
       print(f" -> Generato: {filename}")


   print("\nGenerazione Grafici per Seed...")
   plot_seeds(collected_data['System_Ts'], "System Response Time E[Ts]", f"Transient_System_Ts_Seeds{filename_suffix}.png", "Tempo (s)")
   plot_seeds(collected_data['XRay_Ts'], "XRay Response Time E[Ts]", f"Transient_XRay_Ts_Seeds{filename_suffix}.png", "Tempo (s)")
   plot_seeds(collected_data['XRay_Nq'], "XRay Queue Length E[Nq]", f"Transient_XRay_Nq_Seeds{filename_suffix}.png", "Utenti in Coda")


   # =================================================================================
   # PARTE 2: CENTER COMPARISON (CORRETTA)
   # =================================================================================
   print(f"\nGenerazione Grafici Comparativi Centri (basati su Run {RUN_FOR_CENTER_COMPARISON})...")


   csv_path_run0 = os.path.join(runs_dir, f"run-{RUN_FOR_CENTER_COMPARISON}", "sample.csv")


   if os.path.exists(csv_path_run0):
       try:
           df0 = pd.read_csv(csv_path_run0, sep=',')
           df0.columns = df0.columns.str.strip()
           df0['Metric'] = df0['Metric'].str.strip()
           df0['Center'] = df0['Center'].str.strip()


           def plot_all_centers(metric_name, title, filename, y_label):
               plt.figure(figsize=(12, 7))


               # FILTRO CORRETTO: Qui usiamo 'NumTotal' invece di 'NumSystem'
               df_metric = df0[df0['Metric'] == metric_name]


               unique_centers = df_metric['Center'].unique()
               unique_centers = [c for c in unique_centers if c.lower() != 'system']


               colors = plt.cm.Set2(range(len(unique_centers)))


               plotted_something = False
               for idx, center in enumerate(unique_centers):
                   subset = df_metric[df_metric['Center'] == center]
                   if not subset.empty:
                       plt.plot(subset['Time'], subset['Value'], label=center,
                                color=colors[idx], linewidth=2)
                       plotted_something = True


               if plotted_something:
                   plt.title(f"Transient Analysis (Centers - Run {RUN_FOR_CENTER_COMPARISON}): {title}", fontsize=14, fontweight='bold')
                   plt.xlabel("Tempo Simulazione (s)")
                   plt.ylabel(y_label)
                   plt.legend(loc='upper left', title="Centri", bbox_to_anchor=(1, 1))
                   plt.grid(True, linestyle='--', alpha=0.4)
                   plt.tight_layout()
                   plt.savefig(os.path.join(output_dir, filename), dpi=200)
                   print(f" -> Generato: {filename}")
               else:
                   print(f"[SKIP] Nessun dato trovato per {metric_name}")


           # 4. GRAFICO E[Ns] -> Usiamo 'NumTotal' (che è Ns nel tuo CSV)
           plot_all_centers('NumTotal',
                            "Population E[Ns] per Center",
                            f"Transient_AllCenters_Ns{filename_suffix}.png",
                            "Num. Utenti (Ns)")


           # 5. GRAFICO E[Ts] -> Usiamo 'TimeTotal' (che è Ts nel tuo CSV)
           plot_all_centers('TimeTotal',
                            "Response Time E[Ts] per Center",
                            f"Transient_AllCenters_Ts{filename_suffix}.png",
                            "Tempo (s)")


       except Exception as e:
           print(f"ERRORE elaborazione Run {RUN_FOR_CENTER_COMPARISON} per grafici centri: {e}")
   else:
       print(f"[WARNING] Impossibile generare grafici per centri: {csv_path_run0} non trovato.")


   print(f"\n--- Finito. Totale 5 grafici salvati in: {output_dir} ---")


if __name__ == "__main__":
   if len(sys.argv) < 2:
       print("Uso: python scripts/plot_transient.py <cartella_esperimento> [suffisso_opzionale]")
   else:
       path = sys.argv[1]
       suffix = ""
       if len(sys.argv) > 2:
           suffix = "_" + sys.argv[2]
       generate_transient_plots(path, suffix)

