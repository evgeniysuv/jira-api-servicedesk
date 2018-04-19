package printer;

import java.util.Collection;

/**
 * Created by esuv on 4/19/18
 */
public interface PrintService<T> {

    void printReport(Collection<T> collection);
}
