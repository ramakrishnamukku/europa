package com.distelli.europa.ajax;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSetting;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.SslSettings;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;
import com.distelli.webserver.JsonSuccess;

@Log4j
@Singleton
public class SaveSslSettings extends AjaxHelper<EuropaRequestContext> {

    @Inject
    private SettingsDb _settingsDb;

    public SaveSslSettings() {
        this.supportedHttpMethods.add(HTTPMethod.POST);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext) {
        for ( EuropaSetting setting :
                  ajaxRequest.convertContent(SslSettings.class, true)
                  .toEuropaSettings() )
        {
            if ( null == setting.getValue() || setting.getValue().trim().isEmpty()) {
                _settingsDb.delete(setting.getDomain(), EuropaSettingType.SSL, setting.getKey());
            } else {
                _settingsDb.save(setting);
            }
        }

        return JsonSuccess.Success;
    }
}
