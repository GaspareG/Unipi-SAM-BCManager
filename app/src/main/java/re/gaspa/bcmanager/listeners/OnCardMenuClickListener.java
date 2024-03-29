package re.gaspa.bcmanager.listeners;

import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import re.gaspa.bcmanager.R;
import re.gaspa.bcmanager.ui.adapters.BusinessCardAdapter;
import re.gaspa.bcmanager.ui.models.BusinessCard;
import re.gaspa.bcmanager.utils.Database;

public class OnCardMenuClickListener implements Toolbar.OnMenuItemClickListener
{
    private Context context;
    private BusinessCard businessCard;
    private BusinessCardAdapter businessCardAdapter;

    public OnCardMenuClickListener(BusinessCardAdapter businessCardAdapter, Context ctx, BusinessCard item) {
        this.businessCardAdapter = businessCardAdapter;
        this.businessCard = item;
        this.context = ctx;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();

        menuItem.getMenuInfo();

        if( id == R.id.delete )
        {
            Database.deleteBusinessCard(businessCard);
            Toast.makeText(context, businessCard.getNome() + " " + context.getString(R.string.cancellato), Toast.LENGTH_LONG).show();
            this.businessCardAdapter.setBusinessCardItems(Database.getBusinessCards());
        }
        else if( id == R.id.preferite )
        {
            menuItem.setChecked( !menuItem.isChecked() );
            Database.setPreferite(businessCard, menuItem.isChecked() );
            Toast.makeText(context, businessCard.getNome() + " " + context.getString(R.string.aggiunto_preferiti), Toast.LENGTH_LONG).show();
        }

        return true;
    }
}
