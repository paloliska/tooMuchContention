package sk.palo.liska;

/**
 * @author pavol.liska
 * @date 6/18/2018
 */
public interface IService {

    Long saveTable(MyTable request);

    void updateTable(MyTable request);
    MyTable getTable(Long id);
    Long getCount();
    Long getCountWithTxTemplate();
}
