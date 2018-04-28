package printer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Created by esuv on 4/19/18
 */
public class JasperPrintService<T> implements PrintService<T> {

    @Override
    public void setHeaderParams(@Nullable Map<String, String> headerParams) {

    }

    @Override
    public void setFooterParams(@Nullable Map<String, String> footerParams) {

    }

    @Override
    public void printReport(@NotNull Set<T> data) {

    }
}
