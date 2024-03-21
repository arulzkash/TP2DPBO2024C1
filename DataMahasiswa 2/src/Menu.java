import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.*;


public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();


        // atur ukuran window
        window.setSize(480, 560);

        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);

        // isi window
        window.setContentPane(window.mainPanel);

        // ubah warna background
        window.getContentPane().setBackground(Color.white);

        // tampilkan window
        window.setVisible(true);

        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    private Database database;
    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;

    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JLabel kelasLabel;
    private JRadioButton radioButtonC1;
    private JRadioButton radioButtonC2;


    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        //buat objek database
        database = new Database();

        // isi listMahasiswa
//        populateList();


        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());


        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));


        // atur isi combo box
        String[] JenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(JenisKelaminData));


        // sembunyikan button delete
        deleteButton.setVisible(false);


        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex == -1) {
                    insertData();
                } else {
                    updateData();
                }




            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex >= 0) {
                    deleteData();
                }


            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();


                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedKelas = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();




                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);

                // periksa dan atur radio button berdasarkan kelas
                if (selectedKelas.equals("C1")) {
                    radioButtonC1.setSelected(true);
                    radioButtonC2.setSelected(false);
                } else if (selectedKelas.equals("C2")) {
                    radioButtonC1.setSelected(false);
                    radioButtonC2.setSelected(true);
                }



                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");

                // tampilkan button delete
                deleteButton.setVisible(true);

            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Kelas"};

        // Buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            // Isi tabel dengan listMahasiswa
            int i = 0;
            while(resultSet.next()) {
//                Mahasiswa mahasiswa = listMahasiswa.get(i);
                Object[] row = {i + 1, resultSet.getString("nim"), resultSet.getString("nama"), resultSet.getString("jenis_kelamin"), resultSet.getString("kelas")};
                temp.addRow(row);
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return temp;
    }


    public void insertData() {
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = ""; // Inisialisasi kelas

        // Ambil nilai kelas dari radio button
        if (radioButtonC1.isSelected()) {
            kelas = radioButtonC1.getText();
        } else if (radioButtonC2.isSelected()) {
            kelas = radioButtonC2.getText();
        }

        // Periksa apakah ada input yang kosong
        if (nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty() || kelas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Mohon lengkapi semua input!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Periksa apakah NIM sudah ada dalam database
            if (isNIMExist(nim)) {
                JOptionPane.showMessageDialog(null, "NIM sudah ada dalam database!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Jika tidak ada input yang kosong dan NIM belum ada dalam database, lanjutkan dengan proses insert data
                String sql = "INSERT INTO mahasiswa VALUES (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + kelas + "');";
                database.insertUpdateDeleteQuery(sql);

                // Update tabel
                mahasiswaTable.setModel(setTable());
                clearForm();

                System.out.println("Insert berhasil!");
                JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
            }
        }
    }

    // Method to check if NIM already exists in the database
    private boolean isNIMExist(String nim) {
        String sql = "SELECT * FROM mahasiswa WHERE nim = '" + nim + "'";
        ResultSet resultSet = database.selectQuery(sql);
        try {
            return resultSet.next(); // If resultSet has next, means the NIM already exists
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public void updateData() {
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = ""; // Inisialisasi kelas

        // Ambil nilai kelas dari radio button
        if (radioButtonC1.isSelected()) {
            kelas = radioButtonC1.getText();
        } else if (radioButtonC2.isSelected()) {
            kelas = radioButtonC2.getText();
        }

        // Periksa apakah ada input yang kosong
        if (nim.isEmpty() || nama.isEmpty() || jenisKelamin.isEmpty() || kelas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Mohon lengkapi semua input!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            // Jika tidak ada input yang kosong, lanjutkan dengan proses update data
            String sql = "UPDATE mahasiswa SET nama='" + nama + "', jenis_kelamin='" + jenisKelamin + "', kelas='" + kelas + "' WHERE nim='" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            // Update tabel
            mahasiswaTable.setModel(setTable());
            clearForm();

            System.out.println("Update berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil diubah!");
        }
    }


    public void deleteData() {
        // Ambil nim dari baris terpilih
        String nim = nimField.getText();

        // Tampilkan prompt konfirmasi
        int response = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus data?", "Konfirmasi Hapus Data", JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION) {
            // Hapus data dari list
    //        listMahasiswa.remove(selectedIndex);

            // Hapus data dari database
            String sql = "DELETE FROM mahasiswa WHERE nim='" + nim + "'";
            database.insertUpdateDeleteQuery(sql);

            // Update tabel
            mahasiswaTable.setModel(setTable());

            // Bersihkan form
            clearForm();

            // Beri umpan balik
            System.out.println("Delete berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus");
        }
    }

    public void clearForm() {
        // Kosongkan semua texfield, combo box, dan radio button
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        radioButtonC1.setSelected(false);
        radioButtonC2.setSelected(false);




        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;

    }

//    private void populateList() {
//        listMahasiswa.add(new Mahasiswa("2203999", "Amelia Zalfa Julianti", "Perempuan", "C1"));
//        listMahasiswa.add(new Mahasiswa("2202292", "Muhammad Iqbal Fadhilah", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2202346", "Muhammad Rifky Afandi", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2210239", "Muhammad Hanif Abdillah", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2202046", "Nurainun", "Perempuan", "C1"));
//        listMahasiswa.add(new Mahasiswa("2205101", "Kelvin Julian Putra", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2200163", "Rifanny Lysara Annastasya", "Perempuan", "C2"));
//        listMahasiswa.add(new Mahasiswa("2202869", "Revana Faliha Salma", "Perempuan", "C2"));
//        listMahasiswa.add(new Mahasiswa("2209489", "Rakha Dhifiargo Hariadi", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2203142", "Roshan Syalwan Nurilham", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2200311", "Raden Rahman Ismail", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2200978", "Ratu Syahirah Khairunnisa", "Perempuan", "C2"));
//        listMahasiswa.add(new Mahasiswa("2204509", "Muhammad Fahreza Fauzan", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2205027", "Muhammad Rizki Revandi", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2203484", "Arya Aydin Margono", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2200481", "Marvel Ravindra Dioputra", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2209889", "Muhammad Fadlul Hafiizh", "Laki-laki", "C2"));
//        listMahasiswa.add(new Mahasiswa("2206697", "Rifa Sania", "Perempuan", "C1"));
//        listMahasiswa.add(new Mahasiswa("2207260", "Imam Chalish Rafidhul Haque", "Laki-laki", "C1"));
//        listMahasiswa.add(new Mahasiswa("2204343", "Meiva Labibah Putri", "Perempuan", "C1"));
//    }
}
