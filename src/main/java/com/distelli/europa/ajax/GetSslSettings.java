package com.distelli.europa.ajax;

import com.distelli.europa.EuropaRequestContext;
import com.distelli.europa.db.SettingsDb;
import com.distelli.europa.models.EuropaSettingType;
import com.distelli.europa.models.SslSettings;
import com.distelli.webserver.AjaxHelper;
import com.distelli.webserver.AjaxRequest;
import com.distelli.webserver.HTTPMethod;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.log4j.Log4j;

@Log4j
@Singleton
public class GetSslSettings extends AjaxHelper<EuropaRequestContext> {

    @Inject
    private SettingsDb _settingsDb;

    public GetSslSettings() {
        this.supportedHttpMethods.add(HTTPMethod.GET);
    }

    public Object get(AjaxRequest ajaxRequest, EuropaRequestContext requestContext) {
        return SslSettings.fromEuropaSettings(_settingsDb.listRootSettingsByType(EuropaSettingType.SSL));
    }
}
