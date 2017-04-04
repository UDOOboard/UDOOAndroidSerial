package org.udoo.udooserial;

import android.databinding.DataBindingUtil;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import org.udoo.udooserial.databinding.DigitalLayoutItemBinding;

/**
 * Created by harlem88 on 03/04/17.
 */

public class DigitalAdapter extends RecyclerView.Adapter<DigitalAdapter.MyViewHolder> {
    private DigitalModel[] mDataset;
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemDigitalModeClick(int pos, boolean isInput);

        void onItemDigitalValueClick(int pos, boolean value);
    }

    public DigitalAdapter(DigitalModel[] myDataset) {
        mDataset = myDataset;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }


    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater =
                LayoutInflater.from(parent.getContext());
        DigitalLayoutItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.digital_layout_item, parent, false);
        return new MyViewHolder(binding);
    }

    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.bind(mDataset[position]);
        holder.getBinding().rdgroupDigital.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                DigitalModel digitalModel = mDataset[position];
                if ((checkedId == R.id.rd_btn_input && digitalModel.mode != 1) || (checkedId == R.id.rd_btn_output && digitalModel.mode != 2)) {
                    digitalModel.mode = (short) (checkedId == R.id.rd_btn_input ? 1 : 2);
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemDigitalModeClick(position, checkedId == R.id.rd_btn_input);
                    }
                }
            }
        });
        holder.getBinding().digitalValue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataset[position].value = isChecked;
                if (mItemClickListener != null) {
                    mItemClickListener.onItemDigitalValueClick(position, isChecked);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final DigitalLayoutItemBinding binding;

        public MyViewHolder(DigitalLayoutItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DigitalModel item) {
            binding.setDigital(item);
            binding.executePendingBindings();
        }

        public DigitalLayoutItemBinding getBinding() {
            return binding;
        }
    }
}
